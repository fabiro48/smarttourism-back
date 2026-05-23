package com.smarttourism.backend.common.exception;

/**
 * Thrown when an operation is attempted on a reservation whose current state
 * does not allow that operation (e.g. cancelling an already-expired reservation).
 * Extends {@link BusinessRuleException} → HTTP 422 Unprocessable Entity.
 */
public class InvalidReservationStateException extends BusinessRuleException {

    public InvalidReservationStateException(String message) {
        super(message);
    }

    public InvalidReservationStateException(Object currentStatus) {
        super("La reserva no puede ser cancelada en su estado actual: " + currentStatus);
    }
}
