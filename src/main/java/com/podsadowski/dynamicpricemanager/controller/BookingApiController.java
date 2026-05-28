package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.dto.AvailableSlotsResponse;
import com.podsadowski.dynamicpricemanager.dto.PriceQuoteResponse;
import com.podsadowski.dynamicpricemanager.service.DynamicPricingService;
import com.podsadowski.dynamicpricemanager.service.ReservationService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingApiController {

    private final ReservationService reservationService;
    private final DynamicPricingService dynamicPricingService;

    public BookingApiController(ReservationService reservationService, DynamicPricingService dynamicPricingService) {
        this.reservationService = reservationService;
        this.dynamicPricingService = dynamicPricingService;
    }

    @GetMapping("/available-slots")
    public ResponseEntity<?> availableSlots(
            @RequestParam Long employeeId,
            @RequestParam String date,
            @RequestParam Long serviceId) {
        final LocalDate localDate;
        try {
            localDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<String> slotTimes = reservationService.findAvailableSlotTimes(employeeId, localDate, serviceId);
            return ResponseEntity.ok(new AvailableSlotsResponse(
                    dynamicPricingService.priceForSlots(serviceId, localDate, slotTimes)));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorBody(ex.getMessage()));
        }
    }

    public record ErrorBody(String message) {
    }

    @GetMapping(value = "/price-quote", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> priceQuote(
            @RequestParam Long serviceId,
            @RequestParam String date,
            @RequestParam String time) {
        final LocalDate localDate;
        final LocalTime localTime;
        try {
            localDate = LocalDate.parse(date);
            localTime = LocalTime.parse(time);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
        try {
            PriceQuoteResponse quote = dynamicPricingService.quote(serviceId, localDate, localTime);
            return ResponseEntity.ok(quote);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorBody(ex.getMessage()));
        }
    }
}
