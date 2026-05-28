package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.CreateReservationDto;
import com.podsadowski.dynamicpricemanager.dto.PriceQuoteResponse;
import com.podsadowski.dynamicpricemanager.dto.ReservationSummaryDto;
import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.mapper.DtoMapper;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationSpecifications;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {

    private static final List<ReservationStatus> BLOCKING_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED
    );

    private final ReservationRepository reservationRepository;
    private final EmployeeRepository employeeRepository;
    private final SaloonServicesRepository saloonServicesRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;
    private final DynamicPricingService dynamicPricingService;

    static boolean intervalsOverlap(LocalTime aStart, int aMinutes, LocalTime bStart, int bMinutes) {
        long a1 = aStart.toSecondOfDay();
        long a2 = a1 + (long) aMinutes * 60;
        long b1 = bStart.toSecondOfDay();
        long b2 = b1 + (long) bMinutes * 60;
        return a1 < b2 && b1 < a2;
    }

    public List<String> findAvailableSlotTimes(Long employeeId, LocalDate date, Long serviceId) {
        Employee employee = employeeRepository.findByIdWithServices(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        SaloonService service = saloonServicesRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));

        boolean offers = employee.getServices().stream().anyMatch(s -> s.getId().equals(serviceId));
        if (!offers) {
            throw new IllegalArgumentException("Employee does not offer the selected service.");
        }

        LocalTime workStart = employee.getWorkDayStart() != null ? employee.getWorkDayStart() : LocalTime.of(9, 0);
        LocalTime workEnd = employee.getWorkDayEnd() != null ? employee.getWorkDayEnd() : LocalTime.of(17, 0);
        int durationMinutes = service.getDuration();

        if (!workEnd.isAfter(workStart)) {
            return List.of();
        }

        List<Reservation> bookings = reservationRepository
                .findByEmployeeIdAndReservationDateAndStatusInOrderByReservationTimeAsc(employeeId, date, BLOCKING_STATUSES);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<String> slots = new ArrayList<>();
        for (LocalTime slot = workStart; !slot.plusMinutes(durationMinutes).isAfter(workEnd); slot = slot.plusMinutes(durationMinutes)) {
            if (!date.isBefore(today) && date.isEqual(today) && !slot.isAfter(now)) {
                continue;
            }
            boolean blocked = false;
            for (Reservation r : bookings) {
                int bookedDuration = r.getService().getDuration();
                if (intervalsOverlap(slot, durationMinutes, r.getReservationTime(), bookedDuration)) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) {
                slots.add(slot.toString());
            }
        }
        return slots;
    }

    @Transactional
    public void createReservation(CreateReservationDto dto, String clientEmail) {
        AppUser client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        Employee employee = employeeRepository.findByIdWithServices(dto.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found."));
        SaloonService service = saloonServicesRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));

        boolean offers = employee.getServices().stream().anyMatch(s -> s.getId().equals(dto.getServiceId()));
        if (!offers) {
            throw new IllegalArgumentException("Selected employee does not offer this service.");
        }

        LocalDate date = dto.getDate();
        LocalTime start = dto.getStartTime();
        int durationMinutes = service.getDuration();

        if (date == null || start == null) {
            throw new IllegalArgumentException("Provide appointment date and time.");
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Cannot book a date in the past.");
        }
        if (date.isEqual(today) && !start.isAfter(LocalTime.now())) {
            throw new IllegalArgumentException("Choose a future appointment time.");
        }

        LocalTime workStart = employee.getWorkDayStart() != null ? employee.getWorkDayStart() : LocalTime.of(9, 0);
        LocalTime workEnd = employee.getWorkDayEnd() != null ? employee.getWorkDayEnd() : LocalTime.of(17, 0);

        if (start.isBefore(workStart) || start.plusMinutes(durationMinutes).isAfter(workEnd)) {
            throw new IllegalArgumentException("Selected time is outside employee working hours.");
        }

        long minutesFromWorkStart = java.time.Duration.between(workStart, start).toMinutes();
        if (minutesFromWorkStart < 0 || minutesFromWorkStart % durationMinutes != 0) {
            throw new IllegalArgumentException("Selected time is not an available slot for this service.");
        }

        List<Reservation> bookings = reservationRepository
                .findByEmployeeIdAndReservationDateAndStatusInOrderByReservationTimeAsc(dto.getEmployeeId(), date, BLOCKING_STATUSES);
        for (Reservation r : bookings) {
            int bookedDuration = r.getService().getDuration();
            if (intervalsOverlap(start, durationMinutes, r.getReservationTime(), bookedDuration)) {
                throw new IllegalArgumentException("This slot overlaps another booking.");
            }
        }

        Reservation toSave = new Reservation();
        toSave.setClient(client);
        toSave.setEmployee(employee);
        toSave.setService(service);
        toSave.setReservationDate(date);
        toSave.setReservationTime(start);
        toSave.setStatus(ReservationStatus.PENDING);
        toSave.setContactFirstName(dto.getContactFirstName().trim());
        toSave.setContactLastName(dto.getContactLastName().trim());
        toSave.setContactEmail(dto.getContactEmail().trim());
        toSave.setContactPhone(dto.getContactPhone().trim());

        PriceQuoteResponse quote = dynamicPricingService.quote(service.getId(), date, start);
        toSave.setFinalPrice(quote.finalPrice());

        try {
            reservationRepository.save(toSave);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("This slot was just booked. Choose another time.", ex);
        }
    }

    public List<ReservationSummaryDto> listForClient(String clientEmail) {
        return reservationRepository.findByClient_EmailOrderByReservationDateDescReservationTimeDesc(clientEmail).stream()
                .map(r -> dtoMapper.toReservationSummary(r, isCancellableByClient(r), false, false))
                .collect(Collectors.toList());
    }

    public List<ReservationSummaryDto> listForAdmin(Long employeeId, ReservationStatus status, LocalDate dateFrom, LocalDate dateTo) {
        return reservationRepository.findAll(ReservationSpecifications.forAdmin(employeeId, status, dateFrom, dateTo)).stream()
                .map(r -> dtoMapper.toReservationSummary(
                        r,
                        false,
                        r.getStatus() == ReservationStatus.PENDING,
                        isCancellableByAdmin(r)))
                .collect(Collectors.toList());
    }

    private static boolean isCancellableByAdmin(Reservation r) {
        return r.getStatus() == ReservationStatus.PENDING || r.getStatus() == ReservationStatus.CONFIRMED;
    }

    private static boolean isCancellableByClient(Reservation r) {
        if (r.getStatus() != ReservationStatus.PENDING && r.getStatus() != ReservationStatus.CONFIRMED) {
            return false;
        }
        LocalDateTime start = LocalDateTime.of(r.getReservationDate(), r.getReservationTime());
        return start.isAfter(LocalDateTime.now());
    }

    @Transactional
    public void cancelByClient(Long reservationId, String clientEmail) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (!r.getClient().getEmail().equalsIgnoreCase(clientEmail)) {
            throw new IllegalArgumentException("You do not have permission for this reservation.");
        }
        if (r.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("This reservation is already cancelled.");
        }
        if (r.getStatus() != ReservationStatus.PENDING && r.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalArgumentException("This reservation cannot be cancelled.");
        }
        LocalDateTime start = LocalDateTime.of(r.getReservationDate(), r.getReservationTime());
        if (!start.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel a past appointment.");
        }
        r.setStatus(ReservationStatus.CANCELLED);
    }

    @Transactional
    public void confirmByAdmin(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending reservations can be confirmed.");
        }
        r.setStatus(ReservationStatus.CONFIRMED);
    }

    @Transactional
    public void cancelByAdmin(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));
        if (r.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("This reservation is already cancelled.");
        }
        if (!isCancellableByAdmin(r)) {
            throw new IllegalArgumentException("This reservation cannot be cancelled.");
        }
        r.setStatus(ReservationStatus.CANCELLED);
    }
}
