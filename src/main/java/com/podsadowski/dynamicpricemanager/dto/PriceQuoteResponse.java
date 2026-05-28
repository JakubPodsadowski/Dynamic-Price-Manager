package com.podsadowski.dynamicpricemanager.dto;

import java.util.List;

public record PriceQuoteResponse(
        double basePrice,
        double finalPrice,
        List<PriceAdjustmentLine> lines
) {
}
