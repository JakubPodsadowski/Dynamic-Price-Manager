package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.mapper.DtoMapper;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceCancelTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SaloonServicesRepository saloonServicesRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DtoMapper dtoMapper;
    @Mock
    private DynamicPricingService dynamicPricingService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void cancelByClient_success() {
        AppUser client = new AppUser("c@test.local", "x", "CLIENT");
        Reservation r = reservation(LocalDate.now().plusDays(2), ReservationStatus.CONFIRMED);
        r.setClient(client);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        reservationService.cancelByClient(1L, "c@test.local");
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelByClient_rejectsWrongOwner() {
        Reservation r = reservation(LocalDate.now().plusDays(2), ReservationStatus.PENDING);
        r.setClient(new AppUser("other@test.local", "x", "CLIENT"));
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> reservationService.cancelByClient(1L, "c@test.local"))
                .hasMessageContaining("permission");
    }

    @Test
    void cancelByAdmin_successEvenWhenPast() {
        Reservation r = reservation(LocalDate.now().minusDays(1), ReservationStatus.PENDING);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        reservationService.cancelByAdmin(1L);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancelByClient_rejectsPastVisit() {
        AppUser client = new AppUser("c@test.local", "x", "CLIENT");
        Reservation r = reservation(LocalDate.now().minusDays(1), ReservationStatus.CONFIRMED);
        r.setClient(client);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> reservationService.cancelByClient(1L, "c@test.local"))
                .hasMessageContaining("past");
    }

    private static Reservation reservation(LocalDate date, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setReservationDate(date);
        r.setReservationTime(LocalTime.of(10, 0));
        r.setStatus(status);
        r.setEmployee(new Employee());
        r.setService(new SaloonService());
        return r;
    }
}
