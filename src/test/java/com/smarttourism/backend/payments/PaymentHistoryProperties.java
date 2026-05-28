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
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for the payments history endpoint.
 *
 * <p>Validates: Requirements 1.3, 4.2
 */
class PaymentHistoryProperties {

    // ── Property 2: Ordenamiento descendente por fecha de creación ─────────────

    @Property(tries = 100)
    void paymentHistoryPreservesDescendingOrderByCreatedAt(
            @ForAll("sortedPaymentLists") List<Payment> sortedPayments
    ) {
        // Feature: payments-history-endpoint, Property 2: Ordenamiento descendente por fecha de creación

        UUID touristId = sortedPayments.isEmpty()
                ? UUID.randomUUID()
                : sortedPayments.get(0).getReservation().getTourist().getId();

        // Set up mocks
        PaymentRepository paymentRepository = Mockito.mock(PaymentRepository.class);
        PaymentMapper paymentMapper = Mockito.mock(PaymentMapper.class);

        when(paymentRepository.findByTouristIdWithDetails(touristId)).thenReturn(sortedPayments);

        // Configure mapper mock to preserve createdAt from entity
        when(paymentMapper.toResponse(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return PaymentResponse.builder()
                    .id(payment.getId())
                    .createdAt(payment.getCreatedAt())
                    .paymentStatus(payment.getStatus())
                    .amount(payment.getAmount())
                    .transactionReference(payment.getTransactionReference())
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
                    .build();
        });

        // Create service with mocked dependencies (other deps are null since not used)
        PaymentService paymentService = new PaymentService(
                paymentRepository, null, paymentMapper, null, null
        );

        // Act
        List<PaymentResponse> result = paymentService.getPaymentsByTourist(touristId);

        // Assert: each createdAt is >= the next one (descending order)
        assertThat(result).hasSameSizeAs(sortedPayments);

        for (int i = 0; i < result.size() - 1; i++) {
            LocalDateTime current = result.get(i).getCreatedAt();
            LocalDateTime next = result.get(i + 1).getCreatedAt();
            assertThat(current).isAfterOrEqualTo(next);
        }
    }

    @Provide
    Arbitrary<List<Payment>> sortedPaymentLists() {
        return Arbitraries.integers().between(0, 20).flatMap(size -> {
            if (size == 0) {
                return Arbitraries.just(Collections.emptyList());
            }

            UUID touristId = UUID.randomUUID();
            User tourist = User.builder()
                    .id(touristId)
                    .fullName("Tourist Test")
                    .email("tourist@test.com")
                    .password("hashed")
                    .documentNumber("12345678")
                    .role(Role.TOURIST)
                    .build();

            Experience experience = Experience.builder()
                    .id(UUID.randomUUID())
                    .title("Test Experience")
                    .location("Test Location")
                    .category("Adventure")
                    .price(BigDecimal.valueOf(50))
                    .build();

            return Arbitraries.longs()
                    .between(0, 365L * 24 * 60)  // minutes offset range (up to 1 year)
                    .list().ofSize(size)
                    .map(offsets -> {
                        LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 0, 0);

                        List<Payment> payments = IntStream.range(0, size)
                                .mapToObj(i -> {
                                    LocalDateTime createdAt = baseTime.plusMinutes(offsets.get(i));

                                    Reservation reservation = Reservation.builder()
                                            .id(UUID.randomUUID())
                                            .tourist(tourist)
                                            .experience(experience)
                                            .reservationDate(LocalDate.now())
                                            .quantity(1)
                                            .totalAmount(BigDecimal.valueOf(50))
                                            .status(ReservationStatus.CONFIRMED)
                                            .expirationDate(LocalDateTime.now().plusMinutes(15))
                                            .build();

                                    return Payment.builder()
                                            .id(UUID.randomUUID())
                                            .reservation(reservation)
                                            .transactionReference(UUID.randomUUID().toString())
                                            .status(PaymentStatus.APPROVED)
                                            .amount(BigDecimal.valueOf(50))
                                            .createdAt(createdAt)
                                            .paymentDate(createdAt)
                                            .build();
                                })
                                .toList();

                        // Sort descending by createdAt (simulating DB ORDER BY createdAt DESC)
                        return payments.stream()
                                .sorted(Comparator.comparing(Payment::getCreatedAt).reversed())
                                .toList();
                    });
        });
    }
}
