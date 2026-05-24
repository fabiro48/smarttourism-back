package com.smarttourism.backend.reviews.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for creating a review.
 *
 * <p>Includes validation constraints for rating (1-5 range) and required fields.
 *
 * <p>Validates: Requirements 8.1, 8.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    /**
     * Unique identifier of the experience being reviewed.
     * Must not be null.
     */
    @NotNull(message = "El ID de la experiencia es requerido")
    private UUID experienceId;

    /**
     * Rating given by the tourist, on a scale of 1 to 5.
     * Must be between 1 and 5 inclusive (Requirement 8.2).
     */
    @NotNull(message = "El rating es requerido")
    @Min(value = 1, message = "El rating debe ser un entero entre 1 y 5")
    @Max(value = 5, message = "El rating debe ser un entero entre 1 y 5")
    private Integer rating;

    /**
     * Optional comment or feedback from the tourist.
     * Can be null or empty.
     */
    private String comment;
}
