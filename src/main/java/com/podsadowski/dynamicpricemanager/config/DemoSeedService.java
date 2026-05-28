package com.podsadowski.dynamicpricemanager.config;

import com.podsadowski.dynamicpricemanager.dto.PriceQuoteResponse;
import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import com.podsadowski.dynamicpricemanager.service.DynamicPricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Large demo seed simulating a busy salon: 10 clients, 10 services, 5 employees, dense calendar.
 * Profile {@code demo-seed} — {@code scripts/seed-demo.sh}. Idempotent via {@link #SEED_MARKER} on employees.
 */
@Service
@Profile("demo-seed")
public class DemoSeedService {

    private static final Logger log = LoggerFactory.getLogger(DemoSeedService.class);

    public static final String SEED_MARKER = "__SALON_SIM__";
    private static final String SERVICE_TAG = "[sym]";
    private static final String DEMO_CLIENT_PASSWORD = "demo123";

    /** Calendar days in the past (excluding Sundays). */
    private static final int CALENDAR_DAYS_BACK = 42;
    /** Calendar days in the future. */
    private static final int CALENDAR_DAYS_FORWARD = 14;
    /** Minutes between candidate appointment start times. */
    private static final int SLOT_STEP_MINUTES = 30;
    /** Higher value (0–99) yields more booked slots (deterministic pseudo-random). */
    private static final int BOOKING_THRESHOLD = 28;

    private final SaloonServicesRepository saloonServicesRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final DynamicPricingService dynamicPricingService;
    private final PasswordEncoder passwordEncoder;

    public DemoSeedService(
            SaloonServicesRepository saloonServicesRepository,
            EmployeeRepository employeeRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            DynamicPricingService dynamicPricingService,
            PasswordEncoder passwordEncoder) {
        this.saloonServicesRepository = saloonServicesRepository;
        this.employeeRepository = employeeRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.dynamicPricingService = dynamicPricingService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void seedIfNeeded() {
        if (employeeRepository.findAll().stream()
                .anyMatch(e -> e.getSpecialization() != null && e.getSpecialization().startsWith(SEED_MARKER))) {
            log.info("Skipping salon simulation seed: employees with marker {} already exist.", SEED_MARKER);
            return;
        }

        List<SaloonService> services = createServices();
        saloonServicesRepository.saveAll(services);

        List<Employee> employees = createEmployees(services);
        employeeRepository.saveAll(employees);
        employees = employeeRepository.findAll().stream()
                .filter(e -> e.getSpecialization() != null && e.getSpecialization().startsWith(SEED_MARKER))
                .toList();

        List<AppUser> clients = createDemoClients();

        Map<String, List<long[]>> occupied = new HashMap<>();
        List<Reservation> batch = new ArrayList<>();
        int resCount = 0;

        LocalDate from = LocalDate.now().minusDays(CALENDAR_DAYS_BACK);
        LocalDate to = LocalDate.now().plusDays(CALENDAR_DAYS_FORWARD);

        for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
            if (day.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            for (Employee emp : employees) {
                Set<SaloonService> offered = emp.getServices();
                if (offered == null || offered.isEmpty()) {
                    continue;
                }
                LocalTime workEnd = emp.getWorkDayEnd() != null ? emp.getWorkDayEnd() : LocalTime.of(18, 0);
                LocalTime workStart = emp.getWorkDayStart() != null ? emp.getWorkDayStart() : LocalTime.of(9, 0);

                for (LocalTime slot = workStart;
                     slot.isBefore(workEnd);
                     slot = slot.plusMinutes(SLOT_STEP_MINUTES)) {

                    if (!shouldBook(day, emp.getId(), slot)) {
                        continue;
                    }
                    SaloonService chosen = pickFittingService(offered, slot, workEnd);
                    if (chosen == null) {
                        continue;
                    }
                    long startSec = toSecFromMidnight(slot);
                    long endSec = startSec + chosen.getDuration() * 60L;
                    String key = emp.getId() + ":" + day;
                    if (overlaps(occupied, key, startSec, endSec)) {
                        continue;
                    }

                    AppUser client = clients.get(Math.floorMod(hashDayEmpSlot(day, emp.getId(), slot), clients.size()));
                    Reservation r = new Reservation();
                    r.setClient(client);
                    r.setEmployee(emp);
                    r.setService(chosen);
                    r.setReservationDate(day);
                    r.setReservationTime(slot);
                    r.setStatus(pickStatus(day, slot));
                    r.setContactFirstName(nvl(client.getFirstName(), "Client"));
                    r.setContactLastName(nvl(client.getLastName(), "Demo"));
                    r.setContactEmail(client.getEmail());
                    r.setContactPhone(nvl(client.getPhoneNumber(), "+48000000000"));

                    PriceQuoteResponse quote = dynamicPricingService.quote(chosen.getId(), day, slot);
                    r.setFinalPrice(quote.finalPrice());

                    batch.add(r);
                    addInterval(occupied, key, startSec, endSec);
                    resCount++;

                    if (batch.size() >= 200) {
                        reservationRepository.saveAll(batch);
                        batch.clear();
                    }
                }
            }
        }
        if (!batch.isEmpty()) {
            reservationRepository.saveAll(batch);
        }

        log.info("Salon simulation seed complete: {} services {}, {} employees, {} clients, {} reservations.",
                services.size(), SERVICE_TAG, employees.size(), clients.size(), resCount);
    }

    private static String nvl(String v, String d) {
        return v == null || v.isBlank() ? d : v;
    }

    private ReservationStatus pickStatus(LocalDate day, LocalTime slot) {
        LocalDate today = LocalDate.now();
        if (day.isBefore(today)) {
            return Math.floorMod(hashDaySlot(day, slot), 100) < 8 ? ReservationStatus.PENDING : ReservationStatus.CONFIRMED;
        }
        if (day.isAfter(today)) {
            return Math.floorMod(hashDaySlot(day, slot), 100) < 22 ? ReservationStatus.PENDING : ReservationStatus.CONFIRMED;
        }
        return slot.isAfter(LocalTime.now().plusHours(1))
                ? ReservationStatus.PENDING
                : ReservationStatus.CONFIRMED;
    }

    private static int hashDayEmpSlot(LocalDate day, long empId, LocalTime slot) {
        return Objects.hash(day, empId, slot);
    }

    private static int hashDaySlot(LocalDate day, LocalTime slot) {
        return Objects.hash(day, slot);
    }

    private static boolean shouldBook(LocalDate day, long empId, LocalTime slot) {
        long h = day.toEpochDay() * 7919L + empId * 104729L + slot.toSecondOfDay() * 17L;
        return Math.floorMod((int) h, 100) < BOOKING_THRESHOLD;
    }

    private static SaloonService pickFittingService(Set<SaloonService> offered, LocalTime slot, LocalTime workEnd) {
        List<SaloonService> list = new ArrayList<>(offered);
        list.sort(Comparator.comparingInt(SaloonService::getDuration).reversed());
        for (SaloonService s : list) {
            int d = s.getDuration() == null ? 60 : s.getDuration();
            if (!slot.plusMinutes(d).isAfter(workEnd)) {
                return s;
            }
        }
        return null;
    }

    private static long toSecFromMidnight(LocalTime t) {
        return t.toSecondOfDay();
    }

    private static boolean overlaps(Map<String, List<long[]>> occupied, String key, long startSec, long endSec) {
        List<long[]> list = occupied.get(key);
        if (list == null) {
            return false;
        }
        for (long[] iv : list) {
            if (startSec < iv[1] && iv[0] < endSec) {
                return true;
            }
        }
        return false;
    }

    private static void addInterval(Map<String, List<long[]>> occupied, String key, long startSec, long endSec) {
        occupied.computeIfAbsent(key, k -> new ArrayList<>()).add(new long[]{startSec, endSec});
    }

    private List<SaloonService> createServices() {
        List<SaloonService> list = new ArrayList<>();
        list.add(svc("Women's haircut " + SERVICE_TAG, 120, "Classic cut and blow-dry", 60));
        list.add(svc("Men's haircut " + SERVICE_TAG, 70, "Clipper and scissors", 30));
        list.add(svc("Full color " + SERVICE_TAG, 280, "Color and treatment mask", 120));
        list.add(svc("Highlights / balayage " + SERVICE_TAG, 320, "Multi-step technique", 150));
        list.add(svc("Toner / gloss " + SERVICE_TAG, 150, "Color refresh", 45));
        list.add(svc("Styling " + SERVICE_TAG, 100, "Curls or straightening", 45));
        list.add(svc("Occasion updo " + SERVICE_TAG, 180, "Updo / bridal style", 90));
        list.add(svc("Keratin treatment " + SERVICE_TAG, 450, "Smoothing treatment", 180));
        list.add(svc("Gel manicure " + SERVICE_TAG, 110, "Shape and gel polish", 60));
        list.add(svc("SPA pedicure " + SERVICE_TAG, 140, "Soak, scrub and polish", 75));
        return list;
    }

    private static SaloonService svc(String name, double price, String desc, int duration) {
        SaloonService s = new SaloonService();
        s.setName(name);
        s.setPrice(price);
        s.setDescription(desc);
        s.setDuration(duration);
        return s;
    }

    private List<Employee> createEmployees(List<SaloonService> services) {
        String[][] staff = {
                {"Maja", "Kowalska", "Master stylist"},
                {"Oliwier", "Zielonka", "Colorist"},
                {"Nina", "Kowalska", "Stylist"},
                {"Kamil", "Kowal", "Men's barber"},
                {"Zuzia", "Mazur", "Nail technician"}
        };
        List<Employee> out = new ArrayList<>();
        for (int i = 0; i < staff.length; i++) {
            Employee e = new Employee();
            e.setFirstName(staff[i][0]);
            e.setLastName(staff[i][1]);
            e.setSpecialization(SEED_MARKER + " " + staff[i][2]);
            e.setWorkDayStart(LocalTime.of(8, 30));
            e.setWorkDayEnd(LocalTime.of(18, 30));
            Set<SaloonService> set = new HashSet<>();
            for (int k = 0; k < 6; k++) {
                set.add(services.get((i * 2 + k) % services.size()));
            }
            e.setServices(set);
            out.add(e);
        }
        return out;
    }

    private List<AppUser> createDemoClients() {
        String[][] people = {
                {"simulation.client01@local.test", "Anna", "Nowak", "+48501111201"},
                {"simulation.client02@local.test", "Bartek", "Sum", "+48501111202"},
                {"simulation.client03@local.test", "Maja", "Wojtas", "+48501111203"},
                {"simulation.client04@local.test", "Dawid", "Kowal", "+48501111204"},
                {"simulation.client05@local.test", "Ela", "Kowalska", "+48501111205"},
                {"simulation.client06@local.test", "Kuba", "Toczek", "+48501111206"},
                {"simulation.client07@local.test", "Gosia", "Mazur", "+48501111207"},
                {"simulation.client08@local.test", "Henryk", "Marecki", "+48501111208"},
                {"simulation.client09@local.test", "Iga", "Michalak", "+48501111209"},
                {"simulation.client10@local.test", "Jacek", "Konik", "+48501111210"}
        };
        List<AppUser> clients = new ArrayList<>();
        for (String[] p : people) {
            if (userRepository.existsByEmail(p[0])) {
                clients.add(userRepository.findByEmail(p[0]).orElseThrow());
                continue;
            }
            AppUser u = new AppUser(p[0], passwordEncoder.encode(DEMO_CLIENT_PASSWORD), "CLIENT");
            u.setFirstName(p[1]);
            u.setLastName(p[2]);
            u.setPhoneNumber(p[3]);
            clients.add(userRepository.save(u));
        }
        return clients;
    }
}
