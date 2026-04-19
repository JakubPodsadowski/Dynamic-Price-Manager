package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class SaloonServiceManager {
    private final SaloonServicesRepository saloonServicesRepository;
    private final EmployeeService employeeService;

    public void addService(SaloonService service) {
        saloonServicesRepository.save(service);
    }

    public List<SaloonService> getAllServices() {
        return saloonServicesRepository.findAll();
    }

    public List<SaloonService> getServicesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return saloonServicesRepository.findAllById(ids);
    }

    @Transactional
    public void deleteService(Long id) {
        SaloonService service = saloonServicesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No service with ID: " + id));

        List<Employee> allEmployees = employeeService.getEmployees();
        for (Employee employee : allEmployees) {
            if (employee.getServices().contains(service)) {
                employee.getServices().remove(service);
                employeeService.updateEmployee(employee);
            }
        }

        saloonServicesRepository.delete(service);
    }

    public void updateService(SaloonService service) {
        if (saloonServicesRepository.existsById(service.getId())) {
            saloonServicesRepository.save(service);
        } else {
            throw new RuntimeException("No service with ID: " + service.getId());
        }
    }
}
