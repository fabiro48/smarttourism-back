package com.smarttourism.backend.auth.controller;

import com.smarttourism.backend.auth.dto.AuthResponse;
import com.smarttourism.backend.auth.dto.LoginRequest;
import com.smarttourism.backend.auth.dto.RegisterRequest;
import com.smarttourism.backend.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the public authentication endpoints.
 *
 * <p>Both endpoints are intentionally unauthenticated (configured in
 * {@code SecurityConfig}) so that visitors can register and log in without
 * a prior JWT.
 *
 * <p>Validates: Requirements 1.1, 1.4, 2.1
 */
@Tag(name = "auth", description = "Registro y autenticación de usuarios")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new tourist user.
     *
     * <p>Delegates to {@link AuthService#register(RegisterRequest)} which
     * validates uniqueness of email and document number, hashes the password,
     * persists the user and returns a signed JWT.
     *
     * @param request the registration payload; validated with {@code @Valid}
     * @return HTTP 201 with the {@link AuthResponse} (JWT + user info)
     */
    @Operation(summary = "Registrar un nuevo usuario turista")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates an existing user and returns a JWT.
     *
     * <p>Delegates to {@link AuthService#login(LoginRequest)} which verifies
     * credentials via Spring Security's {@code AuthenticationManager} and
     * checks that the account is active.
     *
     * @param request the login payload; validated with {@code @Valid}
     * @return HTTP 200 with the {@link AuthResponse} (JWT + user info)
     */
    @Operation(summary = "Autenticar usuario y obtener JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
