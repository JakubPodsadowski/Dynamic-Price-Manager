package com.podsadowski.dynamicpricemanager.repository;

import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @EntityGraph(attributePaths = {"service"})
    List<Reservation> findByEmployeeIdAndReservationDateAndStatusInOrderByReservationTimeAsc(
            Long employeeId,
            LocalDate reservationDate,
            Collection<ReservationStatus> statuses);

    long countByEmployeeIdAndStatusNot(Long employeeId, ReservationStatus status);

    long countByServiceIdAndStatusNot(Long serviceId, ReservationStatus status);

    @EntityGraph(attributePaths = {"service", "employee", "client"})
    List<Reservation> findByClient_EmailOrderByReservationDateDescReservationTimeDesc(String email);

    @EntityGraph(attributePaths = {"service", "employee", "client"})
    @Override
    Optional<Reservation> findById(Long id);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.reservationDate >= :from AND r.reservationDate <= :to
            AND r.status IN :statuses
            """)
    List<Reservation> findByReservationDateBetweenAndStatusIn(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("statuses") Collection<ReservationStatus> statuses);
}
