package com.podsadowski.dynamicpricemanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalonServiceFormDto {

    private Long id;

    @NotBlank(message = "Service name is required")
    private String name;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;
}
