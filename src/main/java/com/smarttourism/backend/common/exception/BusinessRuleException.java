package com.smarttourism.backend.common.exception;

/**
 * Base exception for domain business rule violations.
 * Maps to HTTP 422 Unprocessable Entity.
 *
 * <p>Subclasses provide more specific semantics:
 * <ul>
 *   <li>{@link InsufficientSlotsException}</li>
 *   <li>{@link InvalidReservationStateException}</li>
 *   <li>{@link PaymentNotAllowedException}</li>
 * </ul>
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
