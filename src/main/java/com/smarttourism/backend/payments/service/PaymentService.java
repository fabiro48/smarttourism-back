package com.smarttourism.backend.payments.service;

import com.smarttourism.backend.common.enums.PaymentStatus;
import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.common.exception.PaymentNotAllowedException;
import com.smarttourism.backend.common.exception.ResourceNotFoundException;
import com.smarttourism.backend.common.exception.UnauthorizedAccessException;
import com.smarttourism.backend.notifications.service.NotificationService;
import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.payments.entity.Payment;
import com.smarttourism.backend.payments.mapper.PaymentMapper;
import com.smarttourism.backend.payments.repository.PaymentRepository;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.reservations.mapper.ReservationMapper;
import com.smarttourism.backend.reservations.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

/**
 * Service layer for managing {@link Payment} entities.
 *
 * <p>Implements the core business logic for payment simulation, including:
 * <ul>
 *   <li>Reservation ownership verification</li>
 *   <li>Reservation state validation (must be PENDING_PAYMENT)</li>
 *   <li>Transaction reference generation</li>
 *   <li>Payment result simulation (random APPROVED/REJECTED)</li>
 *   <li>Reservation status update on APPROVED payment</li>
 *   <li>Transactional consistency</li>
 * </ul>
 *
 * <p>Validates: Requirements 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentMapper paymentMapper;
    private final ReservationMapper reservationMapper;
    private final NotificationService notificationService;
    private final Random random = new Random();

    /**
     * Simulates a payment for a given reservation.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The reservation exists (throws 404 if not)</li>
     *   <li>The reservation belongs to the tourist (throws 403 if not)</li>
     *   <li>The reservation is in PENDING_PAYMENT state (throws 422 if not)</li>
     * </ul>
     *
     * <p>Then, within a transaction:
     * <ul>
     *   <li>Generates a unique transaction reference (UUID)</li>
     *   <li>Simulates a payment result (70% APPROVED, 30% REJECTED)</li>
     *   <li>Creates a Payment record with the simulated status</li>
     *   <li>If APPROVED, updates the reservation to CONFIRMED</li>
     *   <li>Returns the PaymentResponse with all details</li>
     * </ul>
     *
     * <p>Requirement 7.1: Endpoint accepts reservationId
     * <p>Requirement 7.2: Reservation must be PENDING_PAYMENT
     * <p>Requirement 7.3: Only APPROVED status confirms the reservation
     * <p>Requirement 7.4: Verify reservation exists and belongs to tourist
     * <p>Requirement 7.5: Generate unique transactionReference
     * <p>Requirement 7.6: Return PaymentResponse with all details
     * <p>Requirement 7.7: Ensure atomicity with @Transactional
     *
     * @param reservationId the ID of the reservation to pay for
     * @param touristId the ID of the tourist making the payment
     * @return PaymentResponse with payment details and updated reservation info
     * @throws ResourceNotFoundException if reservation does not exist
     * @throws UnauthorizedAccessException if reservation does not belong to tourist
     * @throws PaymentNotAllowedException if reservation is not in PENDING_PAYMENT state
     */
    @Transactional
    public PaymentResponse simulatePayment(UUID reservationId, UUID touristId) {
        // Requirement 7.4: Verify reservation exists
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        // Requirement 7.4: Verify reservation belongs to tourist
        if (!reservation.getTourist().getId().equals(touristId)) {
            throw new UnauthorizedAccessException("No tienes permiso para pagar esta reserva");
        }

        // Requirement 7.2: Verify reservation is in PENDING_PAYMENT state
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            throw new PaymentNotAllowedException(reservation.getStatus());
        }

        // Requirement 7.5: Generate unique transaction reference
        String transactionReference = UUID.randomUUID().toString();

        // Simulate payment result: 70% APPROVED, 30% REJECTED
        PaymentStatus paymentStatus = random.nextDouble() < 0.7 ? PaymentStatus.APPROVED : PaymentStatus.REJECTED;

        // Create Payment record
        Payment payment = Payment.builder()
                .reservation(reservation)
                .transactionReference(transactionReference)
                .status(paymentStatus)
                .amount(reservation.getTotalAmount())
                .build();

        payment = paymentRepository.save(payment);

        // Requirement 7.3: Only APPROVED status confirms the reservation
        if (paymentStatus == PaymentStatus.APPROVED) {
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservationRepository.save(reservation);
            log.info("Reservation {} confirmed by approved payment {}", reservationId, payment.getId());
        } else {
            log.info("Payment {} rejected for reservation {}", payment.getId(), reservationId);
        }

        // Requirement 7.6: Return PaymentResponse with all details
        PaymentResponse response = paymentMapper.toResponse(payment);
        notificationService.sendPaymentResultEmail(
                reservation.getTourist().getEmail(),
                response,
                reservationMapper.toResponse(reservation)
        );
        return response;
    }
}
