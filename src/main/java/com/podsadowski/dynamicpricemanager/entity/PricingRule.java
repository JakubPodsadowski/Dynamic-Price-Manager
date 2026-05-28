package com.podsadowski.dynamicpricemanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "pricing_rules")
@Getter
@Setter
@NoArgsConstructor
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, length = 160)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime windowStart;

    @Column(nullable = false)
    private LocalTime windowEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PricingRuleKind kind;

    /**
     * BUSY_SURCHARGE: positive percent added to base price.
     * QUIET_DISCOUNT: positive percent subtracted from base price.
     */
    @Column(nullable = false)
    private double percent;
}
