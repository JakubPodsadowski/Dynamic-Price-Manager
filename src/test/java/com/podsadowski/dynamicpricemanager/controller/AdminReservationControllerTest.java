package com.podsadowski.dynamicpricemanager.controller;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AdminReservationControllerTest {

    @Test
    void buildListUrl_includesDateFilters() {
        String url = AdminReservationController.buildListUrl(
                "2",
                "PENDING",
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30));
        assertThat(url).contains("dateFrom=2026-06-01");
        assertThat(url).contains("dateTo=2026-06-30");
        assertThat(url).contains("employeeId=2");
        assertThat(url).contains("status=PENDING");
    }

    @Test
    void buildListUrl_omitsAllStatus() {
        String url = AdminReservationController.buildListUrl(null, "ALL", null, null);
        assertThat(url).isEqualTo("/admin/reservations");
    }
}
