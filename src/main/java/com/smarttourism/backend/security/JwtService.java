package com.smarttourism.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for generating, validating and parsing JWT tokens.
 *
 * <p>Tokens are signed with HMAC-SHA256 using the secret read from the
 * {@code JWT_SECRET} environment variable. The expiration window is
 * controlled by {@code JWT_EXPIRATION_MS} (defaults to 24 h).
 *
 * <p>Validates: Requirements 2.1, 2.5, 2.6 / Property 3
 */
@Slf4j
@Service
public class JwtService {

    private static final String CLAIM_ROLE = "role";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-ms:86400000}")
    private long expirationMs;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generates a signed JWT for the given user.
     *
     * <p>The token subject is the user's email (username). The {@code role}
     * claim contains the first authority stripped of the {@code ROLE_} prefix
     * so that downstream consumers receive a clean value such as {@code TOURIST}
     * or {@code ADMIN}.
     *
     * @param userDetails the authenticated user
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .orElse("");

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    /**
     * Validates a token against the given user details.
     *
     * <p>Checks that the token is well-formed, the signature is valid, the
     * token has not expired, and the subject matches the user's email.
     *
     * @param token       the JWT string to validate
     * @param userDetails the user to validate against
     * @return {@code true} if the token is valid for the user
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extracts the subject (email) from a JWT token.
     *
     * @param token the JWT string
     * @return the email stored as the token subject
     * @throws JwtException if the token is malformed or the signature is invalid
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
