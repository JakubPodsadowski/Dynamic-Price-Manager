package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.CreateReservationDto;
import com.podsadowski.dynamicpricemanager.dto.PriceQuoteResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

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

    private SaloonService service;
    private Employee employee;
    private AppUser client;

    @BeforeEach
    void setUp() {
        service = new SaloonService();
        service.setId(1L);
        service.setName("Cut");
        service.setPrice(100.0);
        service.setDuration(60);

        employee = new Employee();
        employee.setId(2L);
        employee.setFirstName("E");
        employee.setLastName("M");
        employee.setWorkDayStart(LocalTime.of(9, 0));
        employee.setWorkDayEnd(LocalTime.of(17, 0));
        employee.setServices(Set.of(service));

        client = new AppUser("c@test.local", "x", "CLIENT");
        client.setId(3L);
    }

    @Test
    void findAvailableSlotTimes_returnsFreeSlots() {
        LocalDate date = LocalDate.now().plusDays(3);
        when(employeeRepository.findByIdWithServices(2L)).thenReturn(Optional.of(employee));
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));
        when(reservationRepository.findByEmployeeIdAndReservationDateAndStatusInOrderByReservationTimeAsc(
                2L, date, List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)))
                .thenReturn(List.of());

        List<String> slots = reservationService.findAvailableSlotTimes(2L, date, 1L);
        assertThat(slots).contains("09:00", "10:00");
    }

    @Test
    void createReservation_rejectsPastDate() {
        when(userRepository.findByEmail("c@test.local")).thenReturn(Optional.of(client));
        when(employeeRepository.findByIdWithServices(2L)).thenReturn(Optional.of(employee));
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));

        CreateReservationDto dto = validDto(LocalDate.now().minusDays(1), LocalTime.of(10, 0));
        assertThatThrownBy(() -> reservationService.createReservation(dto, "c@test.local"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    void confirmByAdmin_success() {
        Reservation r = new Reservation();
        r.setId(10L);
        r.setStatus(ReservationStatus.PENDING);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(r));

        reservationService.confirmByAdmin(10L);
        assertThat(r.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmByAdmin_onlyPending() {
        Reservation r = new Reservation();
        r.setId(10L);
        r.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.findById(10L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> reservationService.confirmByAdmin(10L))
                .hasMessageContaining("pending");
    }

    @Test
    void listForAdmin_usesSpecification() {
        when(reservationRepository.findAll(ArgumentMatchers.<Specification<Reservation>>any()))
                .thenReturn(List.of());
        reservationService.listForAdmin(null, null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        verify(reservationRepository).findAll(ArgumentMatchers.<Specification<Reservation>>any());
    }

    private CreateReservationDto validDto(LocalDate date, LocalTime time) {
        CreateReservationDto dto = new CreateReservationDto();
        dto.setServiceId(1L);
        dto.setEmployeeId(2L);
        dto.setDate(date);
        dto.setStartTime(time);
        dto.setContactFirstName("Jan");
        dto.setContactLastName("Kowalski");
        dto.setContactEmail("c@test.local");
        dto.setContactPhone("123");
        return dto;
    }

    @Test
    void createReservation_rejectsEmployeeWithoutService() {
        employee.setServices(Set.of());
        when(userRepository.findByEmail("c@test.local")).thenReturn(Optional.of(client));
        when(employeeRepository.findByIdWithServices(2L)).thenReturn(Optional.of(employee));
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> reservationService.createReservation(
                validDto(LocalDate.now().plusDays(5), LocalTime.of(9, 0)), "c@test.local"))
                .hasMessageContaining("does not offer");
    }

    @Test
    void createReservation_rejectsInvalidSlotAlignment() {
        when(userRepository.findByEmail("c@test.local")).thenReturn(Optional.of(client));
        when(employeeRepository.findByIdWithServices(2L)).thenReturn(Optional.of(employee));
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> reservationService.createReservation(
                validDto(LocalDate.now().plusDays(5), LocalTime.of(9, 30)), "c@test.local"))
                .hasMessageContaining("slot");
    }

    @Test
    void createReservation_savesWithQuotedPrice() {
        LocalDate date = LocalDate.now().plusDays(5);
        when(userRepository.findByEmail("c@test.local")).thenReturn(Optional.of(client));
        when(employeeRepository.findByIdWithServices(2L)).thenReturn(Optional.of(employee));
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));
        when(reservationRepository.findByEmployeeIdAndReservationDateAndStatusInOrderByReservationTimeAsc(
                any(), any(), any())).thenReturn(List.of());
        when(dynamicPricingService.quote(1L, date, LocalTime.of(9, 0)))
                .thenReturn(new PriceQuoteResponse(100, 110, List.of()));

        reservationService.createReservation(validDto(date, LocalTime.of(9, 0)), "c@test.local");

        verify(reservationRepository).save(any(Reservation.class));
    }
}
