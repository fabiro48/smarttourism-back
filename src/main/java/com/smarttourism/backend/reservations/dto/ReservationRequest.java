package com.smarttourism.backend.reservations.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for creating a {@code Reservation}.
 *
 * <p>Validates: Requirements 5.1, 5.2
 */
@Data
public class ReservationRequest {

    /**
     * Identifier of the experience to reserve.
     */
    @NotNull(message = "El identificador de la experiencia es obligatorio")
    private UUID experienceId;

    /**
     * Identifier of the schedule slot to reserve.
     */
    @NotNull(message = "El identificador del horario es obligatorio")
    private UUID scheduleId;

    /**
     * Date on which the tourist intends to attend the experience.
     * Must be a present or future date.
     */
    @NotNull(message = "La fecha de reserva es obligatoria")
    @Future(message = "La fecha de reserva debe ser una fecha futura")
    private LocalDate reservationDate;

    /**
     * Number of slots (people) to reserve.
     * Must be at least 1.
     */
    @Min(value = 1, message = "La cantidad mínima de cupos a reservar es 1")
    private int quantity;
}
