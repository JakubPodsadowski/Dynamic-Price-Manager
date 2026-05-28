package com.podsadowski.dynamicpricemanager.dto;

import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSummaryDto {

    private Long id;
    private String clientAccountEmail;
    private LocalDate reservationDate;
    private LocalTime reservationTime;
    private String serviceName;
    private String employeeName;
    private ReservationStatus status;
    private String statusLabel;
    private String contactFirstName;
    private String contactLastName;
    private String contactEmail;
    private String contactPhone;
    private boolean cancellable;
    private boolean confirmableByAdmin;
    /** Admin may cancel pending or confirmed visits (including past). */
    private boolean cancellableByAdmin;
    /** Catalog service price at display time. */
    private Double serviceBasePrice;
    /** Price stored on the reservation (after pricing rules). */
    private Double finalPrice;
}
