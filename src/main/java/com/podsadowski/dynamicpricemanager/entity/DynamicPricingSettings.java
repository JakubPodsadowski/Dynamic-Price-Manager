package com.podsadowski.dynamicpricemanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dynamic_pricing_settings")
@Getter
@Setter
@NoArgsConstructor
public class DynamicPricingSettings {

    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false)
    private int historyLookbackWeeks = 8;

    /** Apply BUSY rules when reservation count in window is >= this threshold. */
    @Column(nullable = false)
    private int busyIfTotalReservationsGte = 5;

    /** Apply QUIET rules when reservation count in window is <= this threshold. */
    @Column(nullable = false)
    private int quietIfTotalReservationsLte = 1;

    @Column(nullable = false)
    private int lastMinuteWithinHours = 2;

    @Column(nullable = false)
    private double lastMinuteDiscountPercent = 10.0;
}
