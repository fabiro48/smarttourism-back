package com.smarttourism.backend.common.exception;

/**
 * Thrown when a payment attempt is made on a reservation whose state does not
 * allow payment processing (e.g. already CONFIRMED, CANCELLED or EXPIRED).
 * Extends {@link BusinessRuleException} → HTTP 422 Unprocessable Entity.
 */
public class PaymentNotAllowedException extends BusinessRuleException {

    public PaymentNotAllowedException(String message) {
        super(message);
    }

    public PaymentNotAllowedException(Object currentStatus) {
        super("No se puede procesar el pago para una reserva en estado: " + currentStatus);
    }
}
