package com.podsadowski.dynamicpricemanager.mapper;

import com.podsadowski.dynamicpricemanager.dto.*;
import com.podsadowski.dynamicpricemanager.entity.AppUser;
import com.podsadowski.dynamicpricemanager.entity.Employee;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public SalonServiceDto toDto(SaloonService entity) {
        if (entity == null) {
            return null;
        }
        return new SalonServiceDto(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getDuration(),
                entity.getDescription()
        );
    }

    public SaloonService toEntity(SalonServiceFormDto dto) {
        SaloonService s = new SaloonService();
        s.setId(dto.getId());
        s.setName(dto.getName());
        s.setPrice(dto.getPrice());
        s.setDescription(dto.getDescription());
        s.setDuration(dto.getDuration());
        return s;
    }

    public EmployeeDto toDto(Employee entity) {
        if (entity == null) {
            return null;
        }
        EmployeeDto dto = new EmployeeDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setSpecialization(entity.getSpecialization());
        dto.setWorkDayStart(entity.getWorkDayStart());
        dto.setWorkDayEnd(entity.getWorkDayEnd());
        if (entity.getServices() != null) {
            dto.setServiceNames(entity.getServices().stream()
                    .map(SaloonService::getName)
                    .sorted()
                    .collect(Collectors.toList()));
            dto.setServiceIds(entity.getServices().stream()
                    .map(SaloonService::getId)
                    .sorted()
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public UserProfileDto toProfileDto(AppUser user) {
        return new UserProfileDto(
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber()
        );
    }

    public ReservationSummaryDto toReservationSummary(
            Reservation r, boolean cancellable, boolean confirmableByAdmin, boolean cancellableByAdmin) {
        if (r == null) {
            return null;
        }
        String employeeName = (r.getEmployee().getFirstName() + " " + r.getEmployee().getLastName()).trim();
        double base = r.getService().getPrice() != null ? r.getService().getPrice() : 0.0;
        Double stored = r.getFinalPrice();
        return ReservationSummaryDto.builder()
                .id(r.getId())
                .clientAccountEmail(r.getClient() != null ? r.getClient().getEmail() : "")
                .reservationDate(r.getReservationDate())
                .reservationTime(r.getReservationTime())
                .serviceName(r.getService().getName())
                .employeeName(employeeName)
                .status(r.getStatus())
                .statusLabel(r.getStatus().getLabel())
                .contactFirstName(r.getContactFirstName())
                .contactLastName(r.getContactLastName())
                .contactEmail(r.getContactEmail())
                .contactPhone(r.getContactPhone())
                .cancellable(cancellable)
                .confirmableByAdmin(confirmableByAdmin)
                .cancellableByAdmin(cancellableByAdmin)
                .serviceBasePrice(base)
                .finalPrice(stored != null ? stored : base)
                .build();
    }
}
