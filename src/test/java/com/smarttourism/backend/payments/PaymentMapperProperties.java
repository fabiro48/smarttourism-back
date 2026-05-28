package com.smarttourism.backend.payments;

import com.smarttourism.backend.common.enums.PaymentStatus;
import com.smarttourism.backend.common.enums.ReservationStatus;
import com.smarttourism.backend.common.enums.Role;
import com.smarttourism.backend.experiences.entity.Experience;
import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.payments.entity.Payment;
import com.smarttourism.backend.payments.mapper.PaymentMapper;
import com.smarttourism.backend.payments.mapper.PaymentMapperImpl;
import com.smarttourism.backend.reservations.entity.Reservation;
import com.smarttourism.backend.users.entity.User;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for PaymentMapper entity→DTO mapping correctness.
 *
 * <p><b>Validates: Requirements 5.2, 5.3</b>
 */
class PaymentMapperProperties {

    private final PaymentMapper mapper = new PaymentMapperImpl();

    // ── Property 3: Correctitud del mapeo entidad→DTO ─────────────────────────

    @Property(tries = 100)
    @Tag("Feature: payments-history-endpoint")
    @Tag("Property 3: Correctitud del mapeo entidad→DTO")
    void allFlattenedFieldsMatchEntityGraph(@ForAll("payments") Payment payment) {
        // Feature: payments-history-endpoint, Property 3: Correctitud del mapeo entidad→DTO
        // **Validates: Requirements 5.2, 5.3**

        PaymentResponse response = mapper.toResponse(payment);

        // Payment direct fields
        assertThat(response.getId()).isEqualTo(payment.getId());
        assertThat(response.getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(response.getPaymentStatus()).isEqualTo(payment.getStatus());
        assertThat(response.getTransactionReference()).isEqualTo(payment.getTransactionReference());
        assertThat(response.getCreatedAt()).isEqualTo(payment.getCreatedAt());

        // Flattened reservation fields
        assertThat(response.getReservationId()).isEqualTo(payment.getReservation().getId());
        assertThat(response.getReservationStatus()).isEqualTo(payment.getReservation().getStatus());
        assertThat(response.getReservationDate()).isEqualTo(payment.getReservation().getReservationDate());
        assertThat(response.getQuantity()).isEqualTo(payment.getReservation().getQuantity());
        assertThat(response.getTotalAmount()).isEqualByComparingTo(payment.getReservation().getTotalAmount());
        assertThat(response.getExpirationDate()).isEqualTo(payment.getReservation().getExpirationDate());

        // Flattened tourist fields
        assertThat(response.getTouristId()).isEqualTo(payment.getReservation().getTourist().getId());
        assertThat(response.getTouristName()).isEqualTo(payment.getReservation().getTourist().getFullName());
        assertThat(response.getTouristEmail()).isEqualTo(payment.getReservation().getTourist().getEmail());

        // Flattened experience fields
        assertThat(response.getExperienceId()).isEqualTo(payment.getReservation().getExperience().getId());
        assertThat(response.getExperienceTitle()).isEqualTo(payment.getReservation().getExperience().getTitle());
        assertThat(response.getExperienceLocation()).isEqualTo(payment.getReservation().getExperience().getLocation());
    }

    // ── Providers ─────────────────────────────────────────────────────────────

    @Provide
    Arbitrary<Payment> payments() {
        return Combinators.combine(
                tourists(),
                experiences(),
                reservationParts(),
                paymentParts()
        ).as((tourist, experience, resParts, payParts) -> {
            Reservation reservation = Reservation.builder()
                    .id(resParts.id)
                    .tourist(tourist)
                    .experience(experience)
                    .reservationDate(resParts.reservationDate)
                    .quantity(resParts.quantity)
                    .totalAmount(resParts.totalAmount)
                    .status(resParts.status)
                    .expirationDate(resParts.expirationDate)
                    .build();

            return Payment.builder()
                    .id(payParts.id)
                    .reservation(reservation)
                    .transactionReference(payParts.transactionReference)
                    .status(payParts.status)
                    .amount(payParts.amount)
                    .createdAt(payParts.createdAt)
                    .paymentDate(payParts.createdAt)
                    .build();
        });
    }

    private Arbitrary<User> tourists() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20).map(s -> s + "@example.com")
        ).as((id, fullName, email) -> User.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .password("hashed")
                .documentNumber("DOC-" + id.toString().substring(0, 8))
                .role(Role.TOURIST)
                .active(true)
                .build());
    }

    private Arbitrary<Experience> experiences() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
        ).as((id, title, location) -> Experience.builder()
                .id(id)
                .title(title)
                .location(location)
                .category("Test")
                .price(BigDecimal.valueOf(50))
                .active(true)
                .build());
    }

    private Arbitrary<ReservationParts> reservationParts() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.integers().between(1, 365).map(d -> LocalDate.now().plusDays(d)),
                Arbitraries.integers().between(1, 100),
                Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(99999)).ofScale(2),
                Arbitraries.of(ReservationStatus.values()),
                Arbitraries.integers().between(1, 525600).map(m -> LocalDateTime.now().plusMinutes(m))
        ).as(ReservationParts::new);
    }

    private Arbitrary<PaymentParts> paymentParts() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(36),
                Arbitraries.of(PaymentStatus.values()),
                Arbitraries.bigDecimals().between(BigDecimal.ONE, BigDecimal.valueOf(99999)).ofScale(2),
                Arbitraries.integers().between(1, 525600).map(m -> LocalDateTime.now().plusMinutes(m))
        ).as(PaymentParts::new);
    }

    // ── Value holders for combining ──────────────────────────────────────────

    private record ReservationParts(
            UUID id,
            LocalDate reservationDate,
            Integer quantity,
            BigDecimal totalAmount,
            ReservationStatus status,
            LocalDateTime expirationDate
    ) {}

    private record PaymentParts(
            UUID id,
            String transactionReference,
            PaymentStatus status,
            BigDecimal amount,
            LocalDateTime createdAt
    ) {}
}
