package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.entity.DynamicPricingSettings;
import com.podsadowski.dynamicpricemanager.entity.PricingRule;
import com.podsadowski.dynamicpricemanager.entity.PricingRuleKind;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.repository.DynamicPricingSettingsRepository;
import com.podsadowski.dynamicpricemanager.repository.PricingRuleRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.podsadowski.dynamicpricemanager.entity.Reservation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DynamicPricingServiceTest {

    @Mock
    private DynamicPricingSettingsRepository settingsRepository;
    @Mock
    private PricingRuleRepository pricingRuleRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private SaloonServicesRepository saloonServicesRepository;

    @InjectMocks
    private DynamicPricingService dynamicPricingService;

    private SaloonService service;
    private DynamicPricingSettings settings;

    @BeforeEach
    void setUp() {
        service = new SaloonService();
        service.setId(1L);
        service.setPrice(100.0);

        settings = new DynamicPricingSettings();
        settings.setId(DynamicPricingSettings.SINGLETON_ID);
        settings.setHistoryLookbackWeeks(4);
        settings.setBusyIfTotalReservationsGte(3);
        settings.setQuietIfTotalReservationsLte(1);
        settings.setLastMinuteWithinHours(24);
        settings.setLastMinuteDiscountPercent(10);
    }

    @Test
    void quote_appliesBusySurchargeWhenThresholdMet() {
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));
        when(settingsRepository.findById(DynamicPricingSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));

        PricingRule rule = new PricingRule();
        rule.setEnabled(true);
        rule.setName("Friday peak");
        rule.setDayOfWeek(DayOfWeek.FRIDAY);
        rule.setWindowStart(LocalTime.of(14, 0));
        rule.setWindowEnd(LocalTime.of(17, 0));
        rule.setKind(PricingRuleKind.BUSY_SURCHARGE);
        rule.setPercent(20);
        when(pricingRuleRepository.findAllByOrderByIdAsc()).thenReturn(List.of(rule));

        LocalDate friday = LocalDate.of(2026, 6, 12);
        List<Reservation> busyHistory = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Reservation r = new Reservation();
            r.setReservationDate(friday.minusWeeks(i));
            r.setReservationTime(LocalTime.of(15, 0));
            busyHistory.add(r);
        }
        when(reservationRepository.findByReservationDateBetweenAndStatusIn(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq(List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))))
                .thenReturn(busyHistory);

        var quote = dynamicPricingService.quote(1L, friday, LocalTime.of(15, 0));
        assertThat(quote.finalPrice()).isGreaterThan(quote.basePrice());
    }

    @Test
    void priceForSlots_returnsQuotesPerSlot() {
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));
        when(settingsRepository.findById(DynamicPricingSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));
        when(pricingRuleRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        var slots = dynamicPricingService.priceForSlots(1L, LocalDate.of(2026, 6, 8), List.of("09:00", "10:00"));
        assertThat(slots).hasSize(2);
        assertThat(slots.getFirst().finalPrice()).isEqualTo(100.0);
    }

    @Test
    void quote_returnsBaseWhenNoRulesMatch() {
        when(saloonServicesRepository.findById(1L)).thenReturn(Optional.of(service));
        when(settingsRepository.findById(DynamicPricingSettings.SINGLETON_ID)).thenReturn(Optional.of(settings));
        when(pricingRuleRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        LocalDate monday = LocalDate.of(2026, 6, 8);
        var quote = dynamicPricingService.quote(1L, monday, LocalTime.of(10, 0));
        assertThat(quote.finalPrice()).isEqualTo(100.0);
    }
}
