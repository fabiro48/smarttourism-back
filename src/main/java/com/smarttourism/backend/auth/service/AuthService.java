package com.smarttourism.backend.auth.service;

import com.smarttourism.backend.auth.dto.AuthResponse;
import com.smarttourism.backend.auth.dto.LoginRequest;
import com.smarttourism.backend.auth.dto.RegisterRequest;
import com.smarttourism.backend.common.enums.Role;
import com.smarttourism.backend.common.exception.DuplicateResourceException;
import com.smarttourism.backend.common.exception.UnauthorizedAccessException;
import com.smarttourism.backend.security.JwtService;
import com.smarttourism.backend.users.entity.User;
import com.smarttourism.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that handles user registration and authentication.
 *
 * <p>Registration validates uniqueness of {@code email} and
 * {@code documentNumber}, hashes the password with BCrypt, persists the new
 * user with role {@code TOURIST} and {@code active = true}, and returns a
 * signed JWT together with the user's basic info.
 *
 * <p>Login delegates credential verification to Spring Security's
 * {@link AuthenticationManager}, checks that the account is active, and
 * returns a fresh JWT on success.
 *
 * <p>Validates: Requirements 1.1, 1.2, 1.3, 1.5, 1.6, 2.1, 2.2, 2.3, 2.4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Registers a new tourist user.
     *
     * <ol>
     *   <li>Validates that neither the {@code email} nor the
     *       {@code documentNumber} is already taken.</li>
     *   <li>Hashes the plain-text password with BCrypt.</li>
     *   <li>Persists the user with role {@code TOURIST} and {@code active = true}.</li>
     *   <li>Generates and returns a signed JWT.</li>
     * </ol>
     *
     * @param request the registration payload
     * @return an {@link AuthResponse} containing the JWT and user info
     * @throws DuplicateResourceException if the email or document number is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate uniqueness of email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("El correo ya está registrado");
        }

        // Validate uniqueness of document number
        if (userRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new DuplicateResourceException("El número de documento ya está registrado");
        }

        // Build and persist the new user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .documentNumber(request.getDocumentNumber())
                .role(Role.TOURIST)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("New user registered: id={}, email={}", savedUser.getId(), savedUser.getEmail());

        String token = jwtService.generateToken(savedUser);
        return buildAuthResponse(token, savedUser);
    }

    /**
     * Authenticates an existing user with email and password.
     *
     * <ol>
     *   <li>Delegates credential verification to {@link AuthenticationManager}
     *       (throws {@link org.springframework.security.core.AuthenticationException}
     *       on failure, which the global handler maps to HTTP 401).</li>
     *   <li>Verifies that the account is active; throws
     *       {@link UnauthorizedAccessException} (HTTP 403) if not.</li>
     *   <li>Generates and returns a signed JWT.</li>
     * </ol>
     *
     * @param request the login payload
     * @return an {@link AuthResponse} containing the JWT and user info
     * @throws UnauthorizedAccessException if the account is inactive
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate — throws AuthenticationException on bad credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = (User) authentication.getPrincipal();

        // Verify the account is active
        if (!user.isEnabled()) {
            throw new UnauthorizedAccessException("La cuenta no está habilitada");
        }

        log.info("User logged in: id={}, email={}", user.getId(), user.getEmail());

        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Builds an {@link AuthResponse} from a token and a persisted user.
     *
     * @param token the signed JWT
     * @param user  the authenticated / registered user
     * @return the assembled response
     */
    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .user(new AuthResponse.UserInfo(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .build();
    }
}
