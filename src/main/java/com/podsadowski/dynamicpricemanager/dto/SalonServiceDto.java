package com.podsadowski.dynamicpricemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalonServiceDto {
    private Long id;
    private String name;
    private Double price;
    private Integer duration;
    private String description;
}
