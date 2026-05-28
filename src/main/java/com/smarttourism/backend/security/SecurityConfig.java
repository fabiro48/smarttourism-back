package com.smarttourism.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * <ul>
 *   <li>CSRF disabled — stateless REST API using JWT.</li>
 *   <li>Session management set to STATELESS.</li>
 *   <li>JWT filter inserted before the default username/password filter.</li>
 *   <li>Route-level authorization rules aligned with the API design.</li>
 * </ul>
 *
 * <p>Validates: Requirements 2.4, 9.4 / Property 12
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // -------------------------------------------------------------------------
    // Security filter chain
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS using the CorsConfigurationSource bean
                .cors(Customizer.withDefaults())

                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless session — no HTTP session is created or used
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ──────────────────────────────────
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/experiences/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/v1/reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/actuator/health").permitAll()

                        // ── Swagger UI / OpenAPI spec (public) ────────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // ── TOURIST endpoints ─────────────────────────────────
                        .requestMatchers(HttpMethod.POST,  "/api/v1/reservations").hasRole("TOURIST")
                        .requestMatchers(HttpMethod.GET,   "/api/v1/reservations/me").hasRole("TOURIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/reservations/*/cancel").hasRole("TOURIST")
                        .requestMatchers(HttpMethod.POST,  "/api/v1/payments/**").hasRole("TOURIST")
                        .requestMatchers(HttpMethod.POST,  "/api/v1/reviews").hasRole("TOURIST")

                        // ── ADMIN endpoints ───────────────────────────────────
                        .requestMatchers(HttpMethod.POST,   "/api/v1/experiences/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/api/v1/experiences/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/experiences/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // ── Everything else requires authentication ───────────
                        .anyRequest().authenticated())

                // Wire in the JWT filter
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Authentication infrastructure beans
    // -------------------------------------------------------------------------

    /**
     * DAO-based authentication provider that uses BCrypt for password
     * verification and our custom {@link UserDetailsServiceImpl}.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the {@link AuthenticationManager} so that {@code AuthService}
     * can programmatically authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder used for hashing and verifying passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
