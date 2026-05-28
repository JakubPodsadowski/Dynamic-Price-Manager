package com.podsadowski.dynamicpricemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String specialization;
    private LocalTime workDayStart;
    private LocalTime workDayEnd;
    private List<String> serviceNames = new ArrayList<>();
    private List<Long> serviceIds = new ArrayList<>();
}
