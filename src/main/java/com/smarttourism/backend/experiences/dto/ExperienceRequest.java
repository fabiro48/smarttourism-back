package com.smarttourism.backend.experiences.dto;

import com.smarttourism.backend.common.enums.Difficulty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request body for creating or updating an {@code Experience}.
 *
 * <p>Validates: Requirements 3.1, 3.2
 */
@Data
public class ExperienceRequest {

    /** Title of the experience. */
    @NotBlank(message = "El título es obligatorio")
    private String title;

    /** Detailed description of the experience. */
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    /** Category (e.g. "Aventura", "Cultural"). */
    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    /** Location where the experience takes place. */
    @NotBlank(message = "La ubicación es obligatoria")
    private String location;

    /** Duration in minutes. */
    @Positive(message = "La duración debe ser un valor positivo")
    private int duration;

    /** Difficulty level of the experience. */
    private Difficulty difficulty;

    /** Price per person in COP. */
    @Positive(message = "El precio debe ser un valor positivo")
    private BigDecimal price;

    /** List of image URLs associated with the experience. */
    private List<String> images;

    /** Latitude of the experience location. */
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser >= -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser <= 90")
    private Double latitude;

    /** Longitude of the experience location. */
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser >= -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser <= 180")
    private Double longitude;
}
