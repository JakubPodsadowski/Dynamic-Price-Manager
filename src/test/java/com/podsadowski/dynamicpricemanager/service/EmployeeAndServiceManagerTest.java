package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.EmployeeSaveDto;
import com.podsadowski.dynamicpricemanager.dto.SalonServiceFormDto;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import com.podsadowski.dynamicpricemanager.repository.UserRepository;
import com.podsadowski.dynamicpricemanager.support.TestEntities;
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
class EmployeeAndServiceManagerTest {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private SaloonServiceManager saloonServiceManager;
    @Autowired
    private SaloonServicesRepository saloonServicesRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void employeeCrud_andDeleteBlockedWithReservations() {
        var service = TestEntities.saveService(saloonServicesRepository, "X", 10, 30);
        EmployeeSaveDto dto = new EmployeeSaveDto();
        dto.setFirstName("T");
        dto.setLastName("E");
        dto.setSpecialization("s");
        dto.setWorkDayStart(LocalTime.of(9, 0));
        dto.setWorkDayEnd(LocalTime.of(17, 0));
        dto.setServiceIds(List.of(service.getId()));
        employeeService.saveOrUpdateEmployee(dto);

        var employees = employeeService.getAllEmployeeDtos();
        assertThat(employees).isNotEmpty();

        Long empId = employeeRepository.findAll().getFirst().getId();
        var client = TestEntities.saveClient(userRepository, passwordEncoder, "emp-del@test.local");
        var employee = employeeRepository.findById(empId).orElseThrow();
        TestEntities.saveReservation(reservationRepository, client, employee, service,
                LocalDate.now().plusDays(2), LocalTime.of(10, 0), ReservationStatus.PENDING);

        assertThatThrownBy(() -> employeeService.deleteEmployee(empId))
                .isInstanceOf(IllegalStateException.class);

        EmployeeSaveDto update = new EmployeeSaveDto();
        update.setId(empId);
        update.setFirstName("Updated");
        update.setLastName("Name");
        update.setWorkDayStart(LocalTime.of(8, 0));
        update.setWorkDayEnd(LocalTime.of(16, 0));
        update.setServiceIds(List.of(service.getId()));
        employeeService.saveOrUpdateEmployee(update);
        assertThat(employeeRepository.findById(empId).orElseThrow().getFirstName()).isEqualTo("Updated");
    }

    @Test
    void serviceCrud_andDelete() {
        SalonServiceFormDto form = new SalonServiceFormDto();
        form.setName("Manicure");
        form.setPrice(60.0);
        form.setDuration(45);
        form.setDescription("d");
        saloonServiceManager.addServiceFromForm(form);

        var list = saloonServiceManager.listAllDtos();
        assertThat(list).anyMatch(s -> "Manicure".equals(s.getName()));

        Long id = saloonServicesRepository.findAll().stream()
                .filter(s -> "Manicure".equals(s.getName()))
                .findFirst()
                .orElseThrow()
                .getId();
        saloonServiceManager.deleteService(id);
        assertThat(saloonServicesRepository.findById(id)).isEmpty();
    }
}
