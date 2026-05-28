package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.dto.PriceAdjustmentLine;
import com.podsadowski.dynamicpricemanager.dto.PriceQuoteResponse;
import com.podsadowski.dynamicpricemanager.dto.SlotPriceDto;
import com.podsadowski.dynamicpricemanager.entity.DynamicPricingSettings;
import com.podsadowski.dynamicpricemanager.entity.PricingRule;
import com.podsadowski.dynamicpricemanager.entity.PricingRuleKind;
import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import com.podsadowski.dynamicpricemanager.entity.SaloonService;
import com.podsadowski.dynamicpricemanager.repository.DynamicPricingSettingsRepository;
import com.podsadowski.dynamicpricemanager.repository.PricingRuleRepository;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import com.podsadowski.dynamicpricemanager.repository.SaloonServicesRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DynamicPricingService {

    private static final List<ReservationStatus> COUNT_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED
    );

    private final DynamicPricingSettingsRepository settingsRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final ReservationRepository reservationRepository;
    private final SaloonServicesRepository saloonServicesRepository;

    public DynamicPricingSettings getSettings() {
        return settingsRepository.findById(DynamicPricingSettings.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Missing dynamic pricing configuration (id=1)."));
    }

    @Transactional
    public void saveSettings(DynamicPricingSettings settings) {
        settingsRepository.save(settings);
    }

    public List<PricingRule> listRules() {
        return pricingRuleRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public PricingRule addRule(PricingRule rule) {
        return pricingRuleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        pricingRuleRepository.deleteById(id);
    }

    @Transactional
    public void updateRule(Long id, PricingRule patch) {
        PricingRule existing = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found."));
        existing.setEnabled(patch.isEnabled());
        existing.setName(patch.getName());
        existing.setDayOfWeek(patch.getDayOfWeek());
        existing.setWindowStart(patch.getWindowStart());
        existing.setWindowEnd(patch.getWindowEnd());
        existing.setKind(patch.getKind());
        existing.setPercent(patch.getPercent());
        pricingRuleRepository.save(existing);
    }

    public PriceQuoteResponse quote(Long serviceId, LocalDate bookingDate, LocalTime bookingTime) {
        SaloonService service = saloonServicesRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Service not found."));
        DynamicPricingSettings cfg = getSettings();
        LocalDateTime now = LocalDateTime.now();

        double base = round2(service.getPrice());
        List<PriceAdjustmentLine> lines = new ArrayList<>();
        double running = base;

        LocalDate todayRef = now.toLocalDate();
        int weeks = Math.max(1, cfg.getHistoryLookbackWeeks());
        LocalDate from = todayRef.minusWeeks((long) weeks);

        for (PricingRule rule : pricingRuleRepository.findAllByOrderByIdAsc()) {
            if (!rule.isEnabled()) {
                continue;
            }
            if (!rule.getWindowEnd().isAfter(rule.getWindowStart())) {
                continue;
            }
            if (bookingDate.getDayOfWeek() != rule.getDayOfWeek()) {
                continue;
            }
            if (!inHalfOpenWindow(bookingTime, rule.getWindowStart(), rule.getWindowEnd())) {
                continue;
            }

            int total = countOccupancy(rule.getDayOfWeek(), rule.getWindowStart(), rule.getWindowEnd(), from, todayRef);
            if (rule.getKind() == PricingRuleKind.BUSY_SURCHARGE && total >= cfg.getBusyIfTotalReservationsGte()) {
                double delta = round2(base * rule.getPercent() / 100.0);
                running += delta;
                lines.add(new PriceAdjustmentLine(
                        rule.getName() + " — high demand (" + total + " visits in window, threshold ≥ "
                                + cfg.getBusyIfTotalReservationsGte() + ")", delta));
            }
            if (rule.getKind() == PricingRuleKind.QUIET_DISCOUNT && total <= cfg.getQuietIfTotalReservationsLte()) {
                double delta = round2(-base * rule.getPercent() / 100.0);
                running += delta;
                lines.add(new PriceAdjustmentLine(
                        rule.getName() + " — low demand (" + total + " visits, threshold ≤ "
                                + cfg.getQuietIfTotalReservationsLte() + ")", delta));
            }
        }

        LocalDateTime slotStart = LocalDateTime.of(bookingDate, bookingTime);
        long minutesUntil = ChronoUnit.MINUTES.between(now, slotStart);
        if (minutesUntil >= 0 && minutesUntil <= (long) cfg.getLastMinuteWithinHours() * 60L) {
            double delta = round2(-base * cfg.getLastMinuteDiscountPercent() / 100.0);
            running += delta;
            lines.add(new PriceAdjustmentLine(
                    "Last-minute discount — visit within ≤ " + cfg.getLastMinuteWithinHours() + " h", delta));
        }

        running = round2(Math.max(0.01, running));
        return new PriceQuoteResponse(base, running, lines);
    }

    public List<SlotPriceDto> priceForSlots(Long serviceId, LocalDate date, List<String> slotIsoTimes) {
        List<SlotPriceDto> out = new ArrayList<>();
        for (String t : slotIsoTimes) {
            LocalTime lt = LocalTime.parse(t);
            PriceQuoteResponse q = quote(serviceId, date, lt);
            out.add(new SlotPriceDto(t, q.finalPrice()));
        }
        return out;
    }

    private int countOccupancy(DayOfWeek dow, LocalTime wStart, LocalTime wEnd, LocalDate from, LocalDate to) {
        List<Reservation> slice = reservationRepository.findByReservationDateBetweenAndStatusIn(from, to, COUNT_STATUSES);
        return (int) slice.stream()
                .filter(r -> r.getReservationDate().getDayOfWeek() == dow)
                .filter(r -> inHalfOpenWindow(r.getReservationTime(), wStart, wEnd))
                .count();
    }

    private static boolean inHalfOpenWindow(LocalTime t, LocalTime start, LocalTime end) {
        return !t.isBefore(start) && t.isBefore(end);
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
