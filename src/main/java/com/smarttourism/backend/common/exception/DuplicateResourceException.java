package com.smarttourism.backend.common.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists
 * (e.g. duplicate email, duplicate document number, duplicate review).
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
