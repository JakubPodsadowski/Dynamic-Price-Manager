package com.podsadowski.dynamicpricemanager.entity;

import lombok.Getter;

@Getter
public enum PricingRuleKind {
    BUSY_SURCHARGE("Busy period surcharge"),
    QUIET_DISCOUNT("Quiet period discount");

    private final String label;

    PricingRuleKind(String label) {
        this.label = label;
    }
}
