package com.podsadowski.dynamicpricemanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateReservationDto {

    @NotNull
    private Long serviceId;

    @NotNull
    private Long employeeId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime startTime;

    @NotBlank(message = "First name is required")
    @Size(max = 120)
    private String contactFirstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 120)
    private String contactLastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255)
    private String contactEmail;

    @NotBlank(message = "Phone number is required")
    @Size(max = 64)
    private String contactPhone;
}
