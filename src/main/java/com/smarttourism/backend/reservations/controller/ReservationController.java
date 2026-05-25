package com.smarttourism.backend.reservations.controller;

import com.smarttourism.backend.reservations.dto.ReservationRequest;
import com.smarttourism.backend.reservations.dto.ReservationResponse;
import com.smarttourism.backend.reservations.service.ReservationService;
import com.smarttourism.backend.users.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller that exposes the reservation management endpoints.
 *
 * <p>All endpoints are restricted to users with the {@code TOURIST} role,
 * enforced both by Spring Security's filter chain (see {@code SecurityConfig})
 * and by {@code @PreAuthorize}.
 *
 * <p>Validates: Requirements 5.1, 5.5, 5.6
 */
@Tag(name = "reservations", description = "Gestión de reservas de turistas")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TOURIST')")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Creates a new reservation for the authenticated tourist.
     *
     * <p>Delegates to {@link ReservationService#createReservation(UUID, ReservationRequest)}
     * which handles pessimistic locking, slot availability verification, and
     * expiration date calculation.
     *
     * @param request the creation payload; validated with {@code @Valid}
     * @param authentication the Spring Security authentication object containing
     *                       the authenticated user's principal
     * @return HTTP 201 with the created {@link ReservationResponse} DTO
     */
    @Operation(summary = "Crear una reserva para el turista autenticado")
    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        UUID touristId = extractUserIdFromAuthentication(authentication);
        ReservationResponse response = reservationService.createReservation(touristId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all reservations for the authenticated tourist.
     *
     * <p>Delegates to {@link ReservationService#getMyReservations(UUID)} which
     * returns reservations ordered by creation date descending.
     *
     * @param authentication the Spring Security authentication object containing
     *                       the authenticated user's principal
     * @return HTTP 200 with a list of {@link ReservationResponse} DTOs
     */
    @Operation(summary = "Obtener mis reservas")
    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            Authentication authentication) {

        UUID touristId = extractUserIdFromAuthentication(authentication);
        List<ReservationResponse> reservations = reservationService.getMyReservations(touristId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Cancels a reservation belonging to the authenticated tourist.
     *
     * <p>Delegates to {@link ReservationService#cancelReservation(UUID, UUID)}
     * which verifies ownership, validates the cancellable state, and restores
     * available slots.
     *
     * @param id the UUID of the reservation to cancel
     * @param authentication the Spring Security authentication object containing
     *                       the authenticated user's principal
     * @return HTTP 204 No Content on success
     */
    @Operation(summary = "Cancelar una reserva propia")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable UUID id,
            Authentication authentication) {

        UUID touristId = extractUserIdFromAuthentication(authentication);
        reservationService.cancelReservation(id, touristId);
        return ResponseEntity.noContent().build();
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
