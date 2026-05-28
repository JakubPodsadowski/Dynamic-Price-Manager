package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.CreateReservationDto;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import com.podsadowski.dynamicpricemanager.support.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationServiceIntegrationTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private SaloonServicesRepository saloonServicesRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long serviceId;
    private Long employeeId;
    private String clientEmail;

    @BeforeEach
    void seed() {
        clientEmail = "res-svc@test.local";
        TestEntities.saveClient(userRepository, passwordEncoder, clientEmail);
        var service = TestEntities.saveService(saloonServicesRepository, "Int", 80, 60);
        serviceId = service.getId();
        employeeId = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "A", "B", service).getId();
    }

    @Test
    void createAndListForClient() {
        LocalDate date = LocalDate.now().plusDays(10);
        CreateReservationDto dto = new CreateReservationDto();
        dto.setServiceId(serviceId);
        dto.setEmployeeId(employeeId);
        dto.setDate(date);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setContactFirstName("Jan");
        dto.setContactLastName("K");
        dto.setContactEmail(clientEmail);
        dto.setContactPhone("123");

        reservationService.createReservation(dto, clientEmail);

        var list = reservationService.listForClient(clientEmail);
        assertThat(list).hasSize(1);
        assertThat(list.getFirst().getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    void findAvailableSlotTimes_excludesBookedSlot() {
        LocalDate date = LocalDate.now().plusDays(10);
        var client = userRepository.findByEmail(clientEmail).orElseThrow();
        var employee = employeeRepository.findById(employeeId).orElseThrow();
        var service = saloonServicesRepository.findById(serviceId).orElseThrow();
        TestEntities.saveReservation(reservationRepository, client, employee, service,
                date, LocalTime.of(9, 0), ReservationStatus.CONFIRMED);

        List<String> slots = reservationService.findAvailableSlotTimes(employeeId, date, serviceId);
        assertThat(slots).doesNotContain("09:00");
        assertThat(slots).contains("10:00");
    }

    @Test
    void createReservation_rejectsOverlappingSlot() {
        LocalDate date = LocalDate.now().plusDays(10);
        var client = userRepository.findByEmail(clientEmail).orElseThrow();
        var employee = employeeRepository.findById(employeeId).orElseThrow();
        var service = saloonServicesRepository.findById(serviceId).orElseThrow();
        TestEntities.saveReservation(reservationRepository, client, employee, service,
                date, LocalTime.of(9, 0), ReservationStatus.CONFIRMED);

        CreateReservationDto dto = new CreateReservationDto();
        dto.setServiceId(serviceId);
        dto.setEmployeeId(employeeId);
        dto.setDate(date);
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setContactFirstName("Jan");
        dto.setContactLastName("K");
        dto.setContactEmail(clientEmail);
        dto.setContactPhone("123");

        assertThatThrownBy(() -> reservationService.createReservation(dto, clientEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
