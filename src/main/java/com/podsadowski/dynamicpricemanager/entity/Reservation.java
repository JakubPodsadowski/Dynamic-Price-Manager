package com.podsadowski.dynamicpricemanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_reservation_employee_slot",
                columnNames = {"employee_id", "reservation_date", "reservation_time"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private AppUser client;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private SaloonService service;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalTime reservationTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReservationStatus status = ReservationStatus.PENDING;

    @Column(name = "contact_first_name", nullable = false, length = 120)
    private String contactFirstName;

    @Column(name = "contact_last_name", nullable = false, length = 120)
    private String contactLastName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = false, length = 64)
    private String contactPhone;

    /** Final price after dynamic pricing at booking time. */
    @Column(name = "final_price")
    private Double finalPrice;
}
