package com.smarttourism.backend.payments.dto;

import com.smarttourism.backend.common.enums.PaymentStatus;
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
 * Response DTO for a payment operation.
 *
 * <p>Includes the payment status, transaction reference, amount, and flattened
 * reservation information for convenience.
 *
 * <p>Validates: Requirements 7.1, 7.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    /** Unique identifier of the payment record. */
    private UUID id;

    /** Status of the payment (PENDING, APPROVED, REJECTED, FAILED). */
    private PaymentStatus paymentStatus;

    /** Unique transaction reference generated for this payment. */
    private String transactionReference;

    /** Amount paid. */
    private BigDecimal amount;

    // ── Reservation info ──────────────────────────────────────────────────────

    /** Unique identifier of the associated reservation. */
    private UUID reservationId;

    /** Current status of the reservation. */
    private ReservationStatus reservationStatus;

    /** Identifier of the tourist who made the reservation. */
    private UUID touristId;

    /** Full name of the tourist. */
    private String touristName;

    /** Email of the tourist. */
    private String touristEmail;

    /** Identifier of the booked experience. */
    private UUID experienceId;

    /** Title of the booked experience. */
    private String experienceTitle;

    /** Location of the booked experience. */
    private String experienceLocation;

    /** Date on which the tourist will attend the experience. */
    private LocalDate reservationDate;

    /** Number of slots reserved. */
    private Integer quantity;

    /** Total amount of the reservation. */
    private BigDecimal totalAmount;

    /** Deadline for payment before automatic expiration. */
    private LocalDateTime expirationDate;

    /** Timestamp when the payment was created. */
    private LocalDateTime createdAt;
}
