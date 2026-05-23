package com.smarttourism.backend.common.exception;

/**
 * Thrown when a date value fails business validation
 * (e.g. reservation date is in the past).
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidDateException extends RuntimeException {

    public InvalidDateException(String message) {
        super(message);
    }
}
