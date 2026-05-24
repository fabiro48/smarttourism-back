package com.smarttourism.backend.reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for a {@code Review} resource.
 *
 * <p>Includes all review fields plus flattened tourist and experience information
 * for convenience in API responses.
 *
 * <p>Validates: Requirements 8.1, 8.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    /** Unique identifier of the review. */
    private UUID id;

    // ── Tourist info ──────────────────────────────────────────────────────────

    /** Identifier of the tourist who wrote the review. */
    private UUID touristId;

    /** Full name of the tourist. */
    private String touristName;

    /** Email of the tourist. */
    private String touristEmail;

    // ── Experience info ───────────────────────────────────────────────────────

    /** Identifier of the reviewed experience. */
    private UUID experienceId;

    /** Title of the reviewed experience. */
    private String experienceTitle;

    // ── Review fields ─────────────────────────────────────────────────────────

    /**
     * Rating given by the tourist, on a scale of 1 to 5.
     * Validated to be in the range [1, 5] (Requirement 8.2).
     */
    private Integer rating;

    /** Optional comment or feedback from the tourist. */
    private String comment;

    /** Timestamp when the review was created. */
    private LocalDateTime createdAt;
}
