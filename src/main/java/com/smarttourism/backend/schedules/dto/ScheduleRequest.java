package com.smarttourism.backend.schedules.dto;

import com.smarttourism.backend.common.enums.DayOfWeek;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalTime;

/**
 * Request body for creating or updating a {@code Schedule}.
 *
 * <p>Validates: Requirements 9.5
 */
@Data
public class ScheduleRequest {

    /** Day of the week when the experience runs. */
    @NotNull(message = "El día de la semana es obligatorio")
    private DayOfWeek dayOfWeek;

    /** Time the experience starts. */
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    /** Time the experience ends. */
    @NotNull(message = "La hora de fin es obligatoria")
    private LocalTime endTime;

    /** Number of slots available for booking. */
    @NotNull(message = "Los cupos disponibles son obligatorios")
    @Positive(message = "Los cupos disponibles deben ser un valor positivo")
    private Integer availableSlots;
}
