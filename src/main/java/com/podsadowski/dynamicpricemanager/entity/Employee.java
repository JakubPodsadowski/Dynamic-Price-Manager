package com.podsadowski.dynamicpricemanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String specialization;

    @Column(nullable = false)
    private LocalTime workDayStart = LocalTime.of(9, 0);

    @Column(nullable = false)
    private LocalTime workDayEnd = LocalTime.of(17, 0);

    @ManyToMany
    @JoinTable(
            name = "employee_services",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<SaloonService> services = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void ensureWorkHoursDefault() {
        if (workDayStart == null) {
            workDayStart = LocalTime.of(9, 0);
        }
        if (workDayEnd == null) {
            workDayEnd = LocalTime.of(17, 0);
        }
    }
}
