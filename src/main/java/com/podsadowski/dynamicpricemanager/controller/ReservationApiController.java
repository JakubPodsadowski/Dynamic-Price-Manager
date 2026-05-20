package com.podsadowski.dynamicpricemanager.controller;

import com.podsadowski.dynamicpricemanager.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/taken-hours")
    public ResponseEntity<List<String>> getTakenHours(@RequestParam Long employeeId, @RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);

        List<LocalTime> takenHours = reservationService.getTakenHoursForEmployeeAndDate(employeeId, localDate);

        List<String> formattedHours = takenHours.stream()
                .map(LocalTime::toString)
                .collect(Collectors.toList());

        return ResponseEntity.ok(formattedHours);
    }
}