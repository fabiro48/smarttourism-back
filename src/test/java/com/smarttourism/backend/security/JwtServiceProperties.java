package com.smarttourism.backend.security;

import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * // Feature: smart-tourism-backend, Property 3: Token JWT contiene rol y es verificable
 *
 * <p>Validates: Requirements 2.1, 2.6
 */
class JwtServiceProperties {

    private static final String TEST_SECRET =
            "dGVzdC1zZWNyZXQta2V5LWZvci10ZXN0aW5nLXB1cnBvc2VzLW9ubHktbXVzdC1iZS1hdC1sZWFzdC0yNTYtYml0cw==";
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtService buildJwtService() {
        JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(service, "expirationMs", EXPIRATION_MS);
        return service;
    }

    // ── Property 3: Token JWT contiene rol y es verificable ───────────────────

    @Property(tries = 100)
    void tokenIsVerifiableForAnyValidEmail(
            @ForAll @NotBlank String email,
            @ForAll @From("roles") String role
    ) {
        // Feature: smart-tourism-backend, Property 3: Token JWT contiene rol y es verificable
        JwtService jwtService = buildJwtService();

        UserDetails userDetails = User.builder()
                .username(email)
                .password("irrelevant")
                .authorities(new SimpleGrantedAuthority("ROLE_" + role))
                .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(jwtService.validateToken(token, userDetails)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
    }

    @Property(tries = 100)
    void tokenForUserAIsInvalidForUserB(
            @ForAll @NotBlank String emailA,
            @ForAll @NotBlank String emailB
    ) {
        // Feature: smart-tourism-backend, Property 3: Token JWT contiene rol y es verificable
        Assume.that(!emailA.equals(emailB));

        JwtService jwtService = buildJwtService();

        UserDetails userA = User.builder().username(emailA).password("x")
                .authorities(new SimpleGrantedAuthority("ROLE_TOURIST")).build();
        UserDetails userB = User.builder().username(emailB).password("x")
                .authorities(new SimpleGrantedAuthority("ROLE_TOURIST")).build();

        String tokenA = jwtService.generateToken(userA);

        assertThat(jwtService.validateToken(tokenA, userB)).isFalse();
    }

    @Provide
    Arbitrary<String> roles() {
        return Arbitraries.of("TOURIST", "ADMIN");
    }
}
