package com.podsadowski.dynamicpricemanager.repository;

import com.podsadowski.dynamicpricemanager.entity.Reservation;
import com.podsadowski.dynamicpricemanager.entity.ReservationStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ReservationSpecifications {

    private ReservationSpecifications() {
    }

    public static Specification<Reservation> forAdmin(
            Long employeeId,
            ReservationStatus status,
            LocalDate dateFrom,
            LocalDate dateTo) {
        return (root, query, cb) -> {
            if (!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())) {
                root.fetch("service", JoinType.LEFT);
                root.fetch("employee", JoinType.LEFT);
                root.fetch("client", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();
            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reservationDate"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("reservationDate"), dateTo));
            }

            query.orderBy(
                    cb.desc(root.get("reservationDate")),
                    cb.desc(root.get("reservationTime"))
            );

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
