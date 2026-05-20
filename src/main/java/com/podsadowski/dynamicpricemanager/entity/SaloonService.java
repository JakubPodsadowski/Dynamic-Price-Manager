package com.podsadowski.dynamicpricemanager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaloonService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Service name can't be blank")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Price is mandatory")
    @Min(value = 0, message = "Price can't be negative")
    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String description;

    @NotNull(message = "Duration is mandatory")
    @Min(value = 1, message = "Duration must exceed minimum 1 minute")
    @Column(nullable = false)
    private Integer duration;

    public SaloonService(String name, Double price, String description, Integer duration) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.duration = duration;
    }
}
