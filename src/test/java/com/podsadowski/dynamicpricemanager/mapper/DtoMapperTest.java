package com.podsadowski.dynamicpricemanager.mapper;

import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMapperTest {

    private final DtoMapper mapper = new DtoMapper();

    @Test
    void toReservationSummary_mapsFields() {
        SaloonService service = new SaloonService();
        service.setName("Cut");
        service.setPrice(90.0);

        Employee employee = new Employee();
        employee.setFirstName("Anna");
        employee.setLastName("Nowak");

        AppUser client = new AppUser("c@test.local", "x", "CLIENT");

        Reservation r = new Reservation();
        r.setId(5L);
        r.setService(service);
        r.setEmployee(employee);
        r.setClient(client);
        r.setReservationDate(LocalDate.of(2026, 6, 1));
        r.setReservationTime(LocalTime.of(10, 30));
        r.setStatus(ReservationStatus.PENDING);
        r.setContactFirstName("Jan");
        r.setContactLastName("K");
        r.setContactEmail("c@test.local");
        r.setContactPhone("123");
        r.setFinalPrice(95.0);

        var dto = mapper.toReservationSummary(r, true, true, true);
        assertThat(dto.getServiceName()).isEqualTo("Cut");
        assertThat(dto.getEmployeeName()).isEqualTo("Anna Nowak");
        assertThat(dto.isCancellable()).isTrue();
        assertThat(dto.isConfirmableByAdmin()).isTrue();
        assertThat(dto.isCancellableByAdmin()).isTrue();
        assertThat(dto.getFinalPrice()).isEqualTo(95.0);
    }

    @Test
    void toDto_nullSafe() {
        assertThat(mapper.toDto((SaloonService) null)).isNull();
        assertThat(mapper.toReservationSummary(null, false, false, false)).isNull();
    }
}
