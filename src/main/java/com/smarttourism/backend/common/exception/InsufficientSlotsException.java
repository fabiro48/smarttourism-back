package com.smarttourism.backend.common.exception;

/**
 * Thrown when a reservation cannot be created because the requested schedule
 * does not have enough available slots.
 * Extends {@link BusinessRuleException} → HTTP 422 Unprocessable Entity.
 */
public class InsufficientSlotsException extends BusinessRuleException {

    public InsufficientSlotsException(String message) {
        super(message);
    }

    public InsufficientSlotsException(int requested, int available) {
        super("No hay cupos disponibles para el horario seleccionado. "
                + "Solicitados: " + requested + ", disponibles: " + available);
    }
}
