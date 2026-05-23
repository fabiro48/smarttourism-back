package com.smarttourism.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Uniform error response body returned by {@code GlobalExceptionHandler} for
 * all 4xx and 5xx responses.
 *
 * <p>Example JSON:
 * <pre>{@code
 * {
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "status": 409,
 *   "error": "Conflict",
 *   "message": "El correo ya está registrado"
 * }
 * }</pre>
 *
 * Validates: Requirement 12.7 / Property 13
 */
@Getter
@Builder
public class ErrorResponse {

    /** ISO-8601 UTC timestamp of when the error occurred. */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private final Instant timestamp;

    /** HTTP status code (e.g. 404, 409, 422). */
    private final int status;

    /** Short HTTP reason phrase (e.g. "Not Found", "Conflict"). */
    private final String error;

    /** Human-readable description of the error. */
    private final String message;
}
