package com.smarttourism.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/login}.
 *
 * <p>Validates: Requirements 2.1, 2.2, 2.3
 */
@Data
public class LoginRequest {

    /** Registered email address of the user. */
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    private String email;

    /** Plain-text password to authenticate against the stored BCrypt hash. */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
