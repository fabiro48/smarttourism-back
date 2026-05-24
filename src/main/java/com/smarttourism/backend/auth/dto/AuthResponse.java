package com.smarttourism.backend.auth.dto;

import com.smarttourism.backend.common.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Response body returned by both {@code POST /api/v1/auth/register} and
 * {@code POST /api/v1/auth/login}.
 *
 * <p>Contains the signed JWT and a minimal projection of the authenticated
 * user so the frontend does not need an extra round-trip.
 *
 * <p>Validates: Requirements 1.1, 1.4, 2.1
 */
@Getter
@Builder
public class AuthResponse {

    /** Signed JWT that the client must include in subsequent requests. */
    private final String token;

    /** Minimal user information embedded in the authentication response. */
    private final UserInfo user;

    /**
     * Immutable projection of the authenticated user's core attributes.
     *
     * <p>Using a Java record keeps the representation concise and
     * naturally immutable without extra Lombok annotations.
     */
    public record UserInfo(
            UUID id,
            String fullName,
            String email,
            Role role
    ) {}
}
