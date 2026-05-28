package com.podsadowski.dynamicpricemanager.entity;

import lombok.Getter;

@Getter
public enum ReservationStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }
}
