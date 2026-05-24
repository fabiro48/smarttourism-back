package com.smarttourism.backend.reservations.dto;

import com.smarttourism.backend.common.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a {@code Reservation} resource.
 *
 * <p>Includes the computed fields {@code totalAmount} and {@code expirationDate}
 * (Requirement 5.2) as well as flattened tourist and experience information for
 * convenience.
 *
 * <p>Validates: Requirements 5.1, 5.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    /** Unique identifier of the reservation. */
    private UUID id;

    // ── Tourist info ──────────────────────────────────────────────────────────

    /** Identifier of the tourist who made the reservation. */
    private UUID touristId;

    /** Full name of the tourist. */
    private String touristName;

    /** Email of the tourist. */
    private String touristEmail;

    // ── Experience info ───────────────────────────────────────────────────────

    /** Identifier of the booked experience. */
    private UUID experienceId;

    /** Title of the booked experience. */
    private String experienceTitle;

    /** Location of the booked experience. */
    private String experienceLocation;

    // ── Schedule info ─────────────────────────────────────────────────────────

    /** Identifier of the schedule slot. */
    private UUID scheduleId;

    // ── Reservation fields ────────────────────────────────────────────────────

    /** Date on which the tourist will attend the experience. */
    private LocalDate reservationDate;

    /** Number of slots reserved. */
    private Integer quantity;

    /**
     * Total amount charged for this reservation ({@code price * quantity}).
     * Computed at creation time (Requirement 5.2).
     */
    private BigDecimal totalAmount;

    /**
     * Current status of the reservation.
     * Possible values: {@code PENDING_PAYMENT}, {@code CONFIRMED},
     * {@code CANCELLED}, {@code EXPIRED}, {@code NO_SHOW}.
     */
    private ReservationStatus status;

    /**
     * Deadline by which the tourist must complete payment before the reservation
     * expires automatically ({@code createdAt + 15 minutes}, Requirement 5.2).
     */
    private LocalDateTime expirationDate;

    /** Timestamp when the reservation was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the reservation was last updated. */
    private LocalDateTime updatedAt;
}
