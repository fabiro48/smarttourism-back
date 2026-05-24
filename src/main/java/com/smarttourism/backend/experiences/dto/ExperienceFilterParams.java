package com.smarttourism.backend.experiences.dto;

import com.smarttourism.backend.common.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Parameters for filtering experiences in search queries.
 * All fields are optional; null values are ignored when building the specification.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienceFilterParams {

    /** Exact match on the category field. */
    private String category;

    /** Case-insensitive LIKE match on the location field. */
    private String location;

    /** Exact match on the difficulty enum field. */
    private Difficulty difficulty;

    /** Minimum price (inclusive) for range filter. */
    private BigDecimal minPrice;

    /** Maximum price (inclusive) for range filter. */
    private BigDecimal maxPrice;

    /** When true, only return experiences with at least one active schedule with available slots. */
    private Boolean available;
}
