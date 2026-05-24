package com.smarttourism.backend.payments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for payment simulation.
 *
 * <p>Contains the reservation identifier that the tourist wishes to pay for.
 *
 * <p>Validates: Requirements 7.1, 7.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    /**
     * Unique identifier of the reservation to be paid.
     * Must not be null.
     */
    @NotNull(message = "El ID de la reserva es requerido")
    private UUID reservationId;
}
