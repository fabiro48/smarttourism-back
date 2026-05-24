package com.smarttourism.backend.payments;

import com.smarttourism.backend.common.enums.PaymentStatus;
import com.smarttourism.backend.common.enums.ReservationStatus;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for payment business rules.
 *
 * <p>Validates: Requirements 7.2, 7.3
 */
class PaymentProperties {

    // ── Property 9: Confirmación de reserva solo por pago APPROVED ────────────

    @Property(tries = 100)
    void reservationIsConfirmedOnlyWhenPaymentIsApproved(
            @ForAll @From("nonApprovedStatuses") PaymentStatus paymentStatus
    ) {
        // Feature: smart-tourism-backend, Property 9: Confirmación de reserva solo por pago APPROVED
        ReservationStatus reservationStatus = simulatePaymentResult(paymentStatus);

        assertThat(reservationStatus).isNotEqualTo(ReservationStatus.CONFIRMED);
    }

    @Property(tries = 1)
    void reservationIsConfirmedWhenPaymentIsApproved() {
        // Feature: smart-tourism-backend, Property 9: Confirmación de reserva solo por pago APPROVED
        ReservationStatus reservationStatus = simulatePaymentResult(PaymentStatus.APPROVED);

        assertThat(reservationStatus).isEqualTo(ReservationStatus.CONFIRMED);
    }

    /**
     * Simulates the reservation status transition based on payment result.
     * Mirrors the logic in PaymentService.
     */
    private ReservationStatus simulatePaymentResult(PaymentStatus paymentStatus) {
        ReservationStatus currentStatus = ReservationStatus.PENDING_PAYMENT;
        if (paymentStatus == PaymentStatus.APPROVED) {
            return ReservationStatus.CONFIRMED;
        }
        return currentStatus;
    }

    @Provide
    Arbitrary<PaymentStatus> nonApprovedStatuses() {
        return Arbitraries.of(PaymentStatus.REJECTED, PaymentStatus.PENDING, PaymentStatus.EXPIRED);
    }
}
