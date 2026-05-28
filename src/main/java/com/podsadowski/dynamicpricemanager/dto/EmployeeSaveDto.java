package com.podsadowski.dynamicpricemanager.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class EmployeeSaveDto {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String specialization;

    @NotNull(message = "Work day start time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime workDayStart = LocalTime.of(9, 0);

    @NotNull(message = "Work day end time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime workDayEnd = LocalTime.of(17, 0);

    private List<Long> serviceIds;

    @AssertTrue(message = "Work day end must be after start")
    public boolean isWorkDayOrderValid() {
        if (workDayStart == null || workDayEnd == null) {
            return true;
        }
        return workDayEnd.isAfter(workDayStart);
    }
}
