package com.smarttourism.backend.experiences.dto;

import com.smarttourism.backend.common.enums.Difficulty;
import com.smarttourism.backend.schedules.dto.ScheduleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for an {@code Experience} resource.
 *
 * <p>Includes computed fields {@code averageRating} and {@code reviewCount} (Requirement 8.5)
 * and the list of active schedules (Requirement 3.2).
 *
 * <p>Validates: Requirements 3.1, 3.2, 8.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceResponse {

    /** Unique identifier of the experience. */
    private UUID id;

    /** Title of the experience. */
    private String title;

    /** Detailed description of the experience. */
    private String description;

    /** Category (e.g. "Aventura", "Cultural"). */
    private String category;

    /** Location where the experience takes place. */
    private String location;

    /** Duration in minutes. */
    private Integer duration;

    /** Difficulty level of the experience. */
    private Difficulty difficulty;

    /** Price per person in COP. */
    private BigDecimal price;

    /** List of image URLs associated with the experience. */
    private List<String> images;

    /** Whether the experience is currently active. */
    private Boolean active;

    /** Timestamp when the experience was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the experience was last updated. */
    private LocalDateTime updatedAt;

    // ── Computed / aggregated fields ──────────────────────────────────────────

    /**
     * Average rating across all reviews for this experience.
     * {@code null} when no reviews exist yet.
     */
    private Double averageRating;

    /** Total number of reviews submitted for this experience. */
    private Integer reviewCount;

    /** Active schedules associated with this experience. */
    private List<ScheduleResponse> schedules;
}
