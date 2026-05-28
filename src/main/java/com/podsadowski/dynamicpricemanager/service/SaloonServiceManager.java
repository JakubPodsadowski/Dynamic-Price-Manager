package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.SalonServiceDto;
import com.podsadowski.dynamicpricemanager.dto.SalonServiceFormDto;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.mapper.DtoMapper;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SaloonServiceManager {

    private final SaloonServicesRepository saloonServicesRepository;
    private final EmployeeService employeeService;
    private final ReservationRepository reservationRepository;
    private final DtoMapper dtoMapper;

    public List<SalonServiceDto> listAllDtos() {
        return saloonServicesRepository.findAll().stream()
                .map(dtoMapper::toDto)
                .collect(Collectors.toList());
    }

    public SalonServiceDto getServiceDtoById(Long id) {
        return dtoMapper.toDto(findEntityById(id));
    }

    public SaloonService findEntityById(Long id) {
        return saloonServicesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with ID: " + id));
    }

    public void addServiceFromForm(SalonServiceFormDto dto) {
        SaloonService entity = dtoMapper.toEntity(dto);
        entity.setId(null);
        saloonServicesRepository.save(entity);
    }

    public void updateServiceFromForm(SalonServiceFormDto dto) {
        SaloonService existing = findEntityById(dto.getId());
        existing.setName(dto.getName());
        existing.setPrice(dto.getPrice());
        existing.setDescription(dto.getDescription());
        existing.setDuration(dto.getDuration());
        saloonServicesRepository.save(existing);
    }

    @Transactional
    public void deleteService(Long id) {
        if (reservationRepository.countByServiceIdAndStatusNot(id, ReservationStatus.CANCELLED) > 0) {
            throw new IllegalStateException(
                    "Cannot delete a service linked to existing reservations.");
        }

        SaloonService service = findEntityById(id);

        List<Employee> allEmployees = employeeService.getEmployees();
        for (Employee employee : allEmployees) {
            if (employee.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()))) {
                employee.getServices().removeIf(s -> s.getId().equals(service.getId()));
                employeeService.saveEmployee(employee);
            }
        }

        saloonServicesRepository.delete(service);
    }
}
