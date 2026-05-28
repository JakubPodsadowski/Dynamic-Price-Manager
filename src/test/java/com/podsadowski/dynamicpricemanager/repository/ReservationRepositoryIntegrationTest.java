package com.podsadowski.dynamicpricemanager.repository;

import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.support.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private SaloonServicesRepository saloonServicesRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void forAdmin_filtersByDateRange() {
        var service = TestEntities.saveService(saloonServicesRepository, "Haircut", 80, 60);
        var employee = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "Anna", "Nowak", service);
        var client = TestEntities.saveClient(userRepository, passwordEncoder, "client@test.local");

        LocalDate inRange = LocalDate.of(2026, 6, 10);
        LocalDate outOfRange = LocalDate.of(2026, 7, 1);
        TestEntities.saveReservation(reservationRepository, client, employee, service,
                inRange, LocalTime.of(10, 0), ReservationStatus.CONFIRMED);
        TestEntities.saveReservation(reservationRepository, client, employee, service,
                outOfRange, LocalTime.of(11, 0), ReservationStatus.CONFIRMED);

        List<com.podsadowski.dynamicpricemanager.entity.Reservation> filtered = reservationRepository.findAll(
                ReservationSpecifications.forAdmin(null, null, inRange, inRange));

        assertThat(filtered).hasSize(1);
        assertThat(filtered.getFirst().getReservationDate()).isEqualTo(inRange);
    }

    @Test
    void forAdmin_filtersByEmployeeAndStatus() {
        var service = TestEntities.saveService(saloonServicesRepository, "Color", 200, 120);
        var anna = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "Anna", "A", service);
        var bob = TestEntities.saveEmployee(employeeRepository, saloonServicesRepository, "Bob", "B", service);
        var client = TestEntities.saveClient(userRepository, passwordEncoder, "c2@test.local");
        LocalDate day = LocalDate.of(2026, 6, 15);

        TestEntities.saveReservation(reservationRepository, client, anna, service,
                day, LocalTime.of(9, 0), ReservationStatus.PENDING);
        TestEntities.saveReservation(reservationRepository, client, bob, service,
                day, LocalTime.of(10, 0), ReservationStatus.CANCELLED);

        var pendingAnna = reservationRepository.findAll(
                ReservationSpecifications.forAdmin(anna.getId(), ReservationStatus.PENDING, null, null));
        assertThat(pendingAnna).hasSize(1);
        assertThat(pendingAnna.getFirst().getEmployee().getId()).isEqualTo(anna.getId());
    }
}
