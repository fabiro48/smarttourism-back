package com.smarttourism.backend.payments;

import com.smarttourism.backend.common.enums.PaymentStatus;
import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.common.enums.Role;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.payments.entity.Payment;
import com.smarttourism.backend.payments.mapper.PaymentMapper;
import com.smarttourism.backend.payments.repository.PaymentRepository;
import com.smarttourism.backend.payments.service.PaymentService;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.users.entity.User;
import net.jqwik.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for tourist data isolation in PaymentService.
 *
 * <p>Validates: Requirements 1.1, 3.1, 3.3
 */
class PaymentServiceIsolationProperties {

    private final PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);
    private final PaymentMapper paymentMapper = new FakePaymentMapper();
    private final PaymentService paymentService;

    PaymentServiceIsolationProperties() {
        paymentService = new PaymentService(
                paymentRepository,
                null, // reservationRepository not needed for this test
                paymentMapper,
                null, // reservationMapper not needed
                null  // notificationService not needed
        );
    }

    // ── Property 1: Aislamiento de datos por turista ──────────────────────────

    @Property(tries = 100)
    @Tag("payments-history-endpoint")
    @Tag("isolation")
    void onlyTouristOwnPaymentsAreReturned(
            @ForAll("touristIds") List<UUID> allTouristIds,
            @ForAll("paymentCounts") List<Integer> paymentCounts
    ) {
        // Feature: payments-history-endpoint, Property 1: Aislamiento de datos por turista
        // **Validates: Requirements 1.1, 3.1, 3.3**

        // Ensure we have at least 2 tourists to test isolation
        if (allTouristIds.size() < 2) return;

        // Pick the target tourist (first one)
        UUID targetTouristId = allTouristIds.get(0);

        // Build payments for each tourist
        List<Payment> allPayments = new java.util.ArrayList<>();
        for (int i = 0; i < allTouristIds.size(); i++) {
            UUID touristId = allTouristIds.get(i);
            int count = paymentCounts.get(i % paymentCounts.size());
            for (int j = 0; j < count; j++) {
                allPayments.add(buildPayment(touristId));
            }
        }

        // Filter only the target tourist's payments (simulating what the repository query does)
        List<Payment> targetPayments = allPayments.stream()
                .filter(p -> p.getReservation().getTourist().getId().equals(targetTouristId))
                .collect(Collectors.toList());

        // Mock repository to return only the target tourist's payments
        when(paymentRepository.findByTouristIdWithDetails(targetTouristId))
                .thenReturn(targetPayments);

        // Act
        List<PaymentResponse> result = paymentService.getPaymentsByTourist(targetTouristId);

        // Assert: all returned payments belong to the target tourist
        assertThat(result).allSatisfy(response ->
                assertThat(response.getTouristId()).isEqualTo(targetTouristId)
        );

        // Assert: the count matches what was generated for the target tourist
        assertThat(result).hasSize(targetPayments.size());
    }

    // ── Providers ─────────────────────────────────────────────────────────────

    @Provide
    Arbitrary<List<UUID>> touristIds() {
        return Arbitraries.create(UUID::randomUUID)
                .list()
                .ofMinSize(2)
                .ofMaxSize(5);
    }

    @Provide
    Arbitrary<List<Integer>> paymentCounts() {
        return Arbitraries.integers().between(0, 4)
                .list()
                .ofMinSize(2)
                .ofMaxSize(5);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Payment buildPayment(UUID touristId) {
        User tourist = User.builder()
                .id(touristId)
                .fullName("Tourist " + touristId.toString().substring(0, 8))
                .email(touristId.toString().substring(0, 8) + "@test.com")
                .password("hashed")
                .documentNumber("DOC-" + touristId.toString().substring(0, 8))
                .role(Role.TOURIST)
                .active(true)
                .build();

        Experience experience = Experience.builder()
                .id(UUID.randomUUID())
                .title("Experience Test")
                .location("Santander")
                .category("Adventure")
                .price(BigDecimal.valueOf(50))
                .active(true)
                .build();

        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .tourist(tourist)
                .experience(experience)
                .reservationDate(LocalDate.now())
                .quantity(1)
                .totalAmount(BigDecimal.valueOf(50))
                .status(ReservationStatus.CONFIRMED)
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        return Payment.builder()
                .id(UUID.randomUUID())
                .reservation(reservation)
                .transactionReference(UUID.randomUUID().toString())
                .status(PaymentStatus.APPROVED)
                .amount(BigDecimal.valueOf(50))
                .createdAt(LocalDateTime.now())
                .paymentDate(LocalDateTime.now())
                .build();
    }

    /**
     * Simple mapper implementation for testing that mirrors the real MapStruct mapper behavior.
     * Maps Payment entity fields to PaymentResponse DTO fields.
     */
    private static class FakePaymentMapper implements PaymentMapper {
        @Override
        public PaymentResponse toResponse(Payment payment) {
            if (payment == null) return null;
            return PaymentResponse.builder()
                    .id(payment.getId())
                    .paymentStatus(payment.getStatus())
                    .transactionReference(payment.getTransactionReference())
                    .amount(payment.getAmount())
                    .reservationId(payment.getReservation().getId())
                    .reservationStatus(payment.getReservation().getStatus())
                    .touristId(payment.getReservation().getTourist().getId())
                    .touristName(payment.getReservation().getTourist().getFullName())
                    .touristEmail(payment.getReservation().getTourist().getEmail())
                    .experienceId(payment.getReservation().getExperience().getId())
                    .experienceTitle(payment.getReservation().getExperience().getTitle())
                    .experienceLocation(payment.getReservation().getExperience().getLocation())
                    .reservationDate(payment.getReservation().getReservationDate())
                    .quantity(payment.getReservation().getQuantity())
                    .totalAmount(payment.getReservation().getTotalAmount())
                    .expirationDate(payment.getReservation().getExpirationDate())
                    .createdAt(payment.getCreatedAt())
                    .build();
        }
    }
}
