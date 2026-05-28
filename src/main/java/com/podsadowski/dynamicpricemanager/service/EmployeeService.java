package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.EmployeeDto;
import com.podsadowski.dynamicpricemanager.dto.EmployeeSaveDto;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.mapper.DtoMapper;
import com.podsadowski.dynamicpricemanager.repository.EmployeeRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final ReservationRepository reservationRepository;
    private final SaloonServicesRepository saloonServicesRepository;
    private final DtoMapper dtoMapper;

    public List<EmployeeDto> getAllEmployeeDtos() {
        return employeeRepository.findAll().stream()
                .map(dtoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveOrUpdateEmployee(EmployeeSaveDto dto) {
        if (dto.getId() == null) {
            Employee employee = new Employee();
            applySaveDto(employee, dto);
            employeeRepository.save(employee);
        } else {
            Employee employee = employeeRepository.findByIdWithServices(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + dto.getId()));
            applySaveDto(employee, dto);
            employeeRepository.save(employee);
        }
    }

    private void applySaveDto(Employee employee, EmployeeSaveDto dto) {
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setSpecialization(dto.getSpecialization());
        employee.setWorkDayStart(dto.getWorkDayStart());
        employee.setWorkDayEnd(dto.getWorkDayEnd());

        if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()) {
            Set<SaloonService> services = new HashSet<>(saloonServicesRepository.findAllById(dto.getServiceIds()));
            employee.setServices(services);
        } else {
            employee.setServices(new HashSet<>());
        }
    }

    public void deleteEmployee(Long id) {
        if (reservationRepository.countByEmployeeIdAndStatusNot(id, ReservationStatus.CANCELLED) > 0) {
            throw new IllegalStateException(
                    "Cannot delete an employee with scheduled reservations. Cancel or reassign appointments first.");
        }
        employeeRepository.deleteById(id);
    }

    public List<Employee> getEmployees() {
        return employeeRepository.findAll();
    }

    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }
}
