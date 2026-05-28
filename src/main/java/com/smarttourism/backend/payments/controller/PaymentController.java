package com.smarttourism.backend.payments.controller;

import com.smarttourism.backend.payments.dto.PaymentRequest;
import com.smarttourism.backend.payments.dto.PaymentResponse;
import com.smarttourism.backend.payments.service.PaymentService;
import com.smarttourism.backend.users.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller that exposes the payment simulation endpoints.
 *
 * <p>All endpoints are restricted to users with the {@code TOURIST} role,
 * enforced both by Spring Security's filter chain (see {@code SecurityConfig})
 * and by {@code @PreAuthorize}.
 *
 * <p>Validates: Requirements 7.1
 */
@Tag(name = "payments", description = "Simulación de pagos de reservas")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TOURIST')")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Simulates a payment for a given reservation.
     *
     * <p>Delegates to {@link PaymentService#simulatePayment(UUID, UUID)} which
     * verifies reservation ownership, validates the reservation state, simulates
     * a payment result, and updates the reservation status if approved.
     *
     * <p>Requirement 7.1: Endpoint accepts reservationId and returns payment result
     *
     * @param request the payment request containing the reservation ID; validated with {@code @Valid}
     * @param authentication the Spring Security authentication object containing
     *                       the authenticated user's principal
     * @return HTTP 200 with the {@link PaymentResponse} containing payment details
     *         and updated reservation information
     */
    @Operation(summary = "Simular el pago de una reserva pendiente")
    @PostMapping("/simulate")
    public ResponseEntity<PaymentResponse> simulatePayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {

        UUID touristId = extractUserIdFromAuthentication(authentication);
        PaymentResponse response = paymentService.simulatePayment(request.getReservationId(), touristId);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns the payment history for the authenticated tourist.
     *
     * <p>Retrieves all payments associated with reservations belonging to the
     * authenticated tourist, ordered by creation date descending (most recent first).
     *
     * <p>Validates: Requirements 1.1, 1.2, 1.3, 2.3
     *
     * @param authentication the Spring Security authentication object containing
     *                       the authenticated user's principal
     * @return HTTP 200 with a list of {@link PaymentResponse} (empty list if no payments)
     */
    @Operation(summary = "Obtener historial de pagos del turista autenticado")
    @GetMapping("/me")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        UUID touristId = extractUserIdFromAuthentication(authentication);
        List<PaymentResponse> payments = paymentService.getPaymentsByTourist(touristId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Extracts the user ID from the Spring Security authentication object.
     *
     * <p>The principal is expected to be a {@link User} entity instance
     * (which implements {@link org.springframework.security.core.userdetails.UserDetails}).
     *
     * @param authentication the Spring Security authentication object
     * @return the UUID of the authenticated user
     * @throws IllegalArgumentException if the principal is not a User instance
     */
    private UUID extractUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalArgumentException("Invalid authentication principal");
        }

        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
