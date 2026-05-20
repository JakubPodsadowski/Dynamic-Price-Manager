package com.podsadowski.dynamicpricemanager.service;

import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public void addReservation(Reservation reservation) {
        reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<LocalTime> getTakenHoursForEmployeeAndDate(Long employeeId, LocalDate date) {
        List<Reservation> dailyReservations = reservationRepository.findByEmployeeIdAndReservationDate(employeeId, date);

        return dailyReservations.stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toList());
    }
}
