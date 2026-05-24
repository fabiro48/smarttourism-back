package com.smarttourism.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/v1/auth/register}.
 *
 * <p>All fields are mandatory. The {@code email} field must also be a
 * well-formed email address.
 *
 * <p>Validates: Requirements 1.1, 1.4
 */
@Data
public class RegisterRequest {

    /** Full name of the user. */
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    /** Email address — must be unique in the system. */
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    private String email;

    /** Plain-text password; will be hashed with BCrypt before persistence. */
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    /** Contact phone number. */
    @NotBlank(message = "El teléfono es obligatorio")
    private String phone;

    /** National identity / document number — must be unique in the system. */
    @NotBlank(message = "El número de documento es obligatorio")
    private String documentNumber;
}
