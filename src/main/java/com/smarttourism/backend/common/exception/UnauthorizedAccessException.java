package com.smarttourism.backend.common.exception;

/**
 * Thrown when an authenticated user attempts to access or modify a resource
 * they do not own or are not permitted to act upon
 * (e.g. cancelling another tourist's reservation).
 * Maps to HTTP 403 Forbidden.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
