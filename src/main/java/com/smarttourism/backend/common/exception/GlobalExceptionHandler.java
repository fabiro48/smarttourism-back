package com.smarttourism.backend.common.exception;

import com.smarttourism.backend.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralized exception handler that converts domain and framework exceptions
 * into a uniform {@link ErrorResponse} JSON body.
 *
 * <p>No stack traces are exposed to the client in any environment.
 *
 * <p>Validates: Requirement 12.7 / Property 13
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // 404 Not Found
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 409 Conflict
    // -------------------------------------------------------------------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 422 Unprocessable Entity  (BusinessRuleException and all subclasses)
    // -------------------------------------------------------------------------

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    // -------------------------------------------------------------------------
    // 400 Bad Request
    // -------------------------------------------------------------------------

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDate(InvalidDateException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles Bean Validation failures ({@code @Valid} on request bodies).
     * Collects all field-level errors into a single comma-separated message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    // -------------------------------------------------------------------------
    // 403 Forbidden
    // -------------------------------------------------------------------------

    /**
     * Handles domain-level ownership/permission violations
     * (e.g. tourist trying to cancel another tourist's reservation).
     */
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Handles Spring Security role-based access denials
     * (e.g. TOURIST accessing an ADMIN endpoint).
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Acceso denegado: no tienes permisos para realizar esta operación");
    }

    // -------------------------------------------------------------------------
    // 401 Unauthorized
    // -------------------------------------------------------------------------

    /**
     * Handles Spring Security authentication failures
     * (e.g. invalid or expired JWT token).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, "Token de autenticación inválido o expirado");
    }

    // -------------------------------------------------------------------------
    // 500 Internal Server Error  (catch-all)
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno. Por favor, inténtalo más tarde.");
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
