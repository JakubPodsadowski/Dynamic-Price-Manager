package com.podsadowski.dynamicpricemanager.service;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationServiceOverlapTest {

    @Test
    void overlapsWhenRangesTouchInside() {
        assertTrue(ReservationService.intervalsOverlap(LocalTime.of(9, 0), 120, LocalTime.of(10, 0), 60));
    }

    @Test
    void noOverlapWhenBackToBack() {
        assertFalse(ReservationService.intervalsOverlap(LocalTime.of(9, 0), 60, LocalTime.of(10, 0), 30));
    }
}
