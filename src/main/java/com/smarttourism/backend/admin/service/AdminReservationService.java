package com.smarttourism.backend.admin.service;

import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.reservations.mapper.ReservationMapper;
import com.smarttourism.backend.reservations.dto.ReservationResponse;
import com.smarttourism.backend.reservations.repository.ReservationRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for admin-level reservation management.
 *
 * <p>Provides filtering and pagination for all reservations in the system.
 *
 * <p>Validates: Requirement 9.6
 */
@Service
@RequiredArgsConstructor
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    /**
     * Retrieves all reservations with optional filtering.
     *
     * @param status        optional status filter
     * @param experienceId  optional experience ID filter
     * @param startDate     optional start date for date range filter
     * @param endDate       optional end date for date range filter
     * @param pageable      pagination parameters
     * @return page of reservation responses
     */
    public Page<ReservationResponse> getAllReservations(
            ReservationStatus status,
            UUID experienceId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        Specification<Reservation> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (experienceId != null) {
                predicates.add(criteriaBuilder.equal(root.get("experience").get("id"), experienceId));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("reservationDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("reservationDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return reservationRepository.findAll(spec, pageable)
                .map(reservationMapper::toResponse);
    }
}
