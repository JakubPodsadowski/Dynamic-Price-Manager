package com.podsadowski.dynamicpricemanager.support;

import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public final class TestEntities {

    private TestEntities() {
    }

    public static SaloonService saveService(SaloonServicesRepository repo, String name, double price, int duration) {
        SaloonService s = new SaloonService();
        s.setName(name);
        s.setPrice(price);
        s.setDuration(duration);
        s.setDescription("Test");
        return repo.save(s);
    }

    public static Employee saveEmployee(
            EmployeeRepository repo,
            SaloonServicesRepository serviceRepo,
            String first,
            String last,
            SaloonService... services) {
        Employee e = new Employee();
        e.setFirstName(first);
        e.setLastName(last);
        e.setSpecialization("test");
        e.setWorkDayStart(LocalTime.of(9, 0));
        e.setWorkDayEnd(LocalTime.of(17, 0));
        if (services.length > 0) {
            e.setServices(Set.of(services));
        }
        return repo.save(e);
    }

    public static AppUser saveClient(UserRepository repo, PasswordEncoder encoder, String email) {
        AppUser u = new AppUser(email, encoder.encode("pass"), "CLIENT");
        u.setFirstName("Jan");
        u.setLastName("Kowalski");
        u.setPhoneNumber("500600700");
        return repo.save(u);
    }

    public static Reservation saveReservation(
            ReservationRepository repo,
            AppUser client,
            Employee employee,
            SaloonService service,
            LocalDate date,
            LocalTime time,
            ReservationStatus status) {
        Reservation r = new Reservation();
        r.setClient(client);
        r.setEmployee(employee);
        r.setService(service);
        r.setReservationDate(date);
        r.setReservationTime(time);
        r.setStatus(status);
        r.setContactFirstName("Jan");
        r.setContactLastName("Kowalski");
        r.setContactEmail(client.getEmail());
        r.setContactPhone("500600700");
        r.setFinalPrice(service.getPrice());
        return repo.save(r);
    }
}
