package com.smarttourism.backend.common;

import com.smarttourism.backend.common.dto.ErrorResponse;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for uniform error response format.
 *
 * <p>Validates: Requirement 12.7
 */
class ErrorFormatProperties {

    // ── Property 13: Formato de error uniforme ────────────────────────────────

    @Property(tries = 100)
    void errorResponseAlwaysContainsRequiredFields(
            @ForAll @IntRange(min = 400, max = 599) int httpStatus,
            @ForAll @NotBlank String errorMessage,
            @ForAll @NotBlank String errorType
    ) {
        // Feature: smart-tourism-backend, Property 13: Formato de error uniforme
        Assume.that(errorMessage.isBlank() == false);
        Assume.that(errorType.isBlank() == false);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(httpStatus)
                .error(errorType.trim())
                .message(errorMessage.trim())
                .build();

        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getStatus()).isBetween(400, 599);
        assertThat(response.getError()).isNotBlank();
        assertThat(response.getMessage()).isNotBlank();
    }

    @Property(tries = 100)
    void errorResponseStatusMatchesHttpCode(
            @ForAll @IntRange(min = 400, max = 599) int httpStatus
    ) {
        // Feature: smart-tourism-backend, Property 13: Formato de error uniforme
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(httpStatus)
                .error("Error")
                .message("Some error message")
                .build();

        assertThat(response.getStatus()).isEqualTo(httpStatus);
    }
}
