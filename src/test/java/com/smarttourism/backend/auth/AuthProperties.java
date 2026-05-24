package com.smarttourism.backend.auth;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Property-based tests for authentication business rules.
 *
 * <p>Validates: Requirements 1.5
 */
class AuthProperties {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ── Property 2: Contraseña almacenada como hash BCrypt ────────────────────

    @Property(tries = 100)
    void passwordIsStoredAsBcryptHash(@ForAll @NotBlank String plainPassword) {
        // Feature: smart-tourism-backend, Property 2: Contraseña almacenada como hash BCrypt
        String hashed = encoder.encode(plainPassword);

        // Hash must start with BCrypt prefix
        assertThat(hashed).startsWith("$2a$");

        // Hash must not equal plain text
        assertThat(hashed).isNotEqualTo(plainPassword);

        // Hash must be verifiable
        assertThat(encoder.matches(plainPassword, hashed)).isTrue();
    }

    @Property(tries = 100)
    void samePasswordProducesDifferentHashes(@ForAll @NotBlank String plainPassword) {
        // Feature: smart-tourism-backend, Property 2: Contraseña almacenada como hash BCrypt
        String hash1 = encoder.encode(plainPassword);
        String hash2 = encoder.encode(plainPassword);

        // BCrypt uses random salt — same input produces different hashes
        assertThat(hash1).isNotEqualTo(hash2);

        // But both must verify correctly
        assertThat(encoder.matches(plainPassword, hash1)).isTrue();
        assertThat(encoder.matches(plainPassword, hash2)).isTrue();
    }
}
