package com.smarttourism.backend.common.exception;

/**
 * Thrown when a requested resource does not exist or is not accessible.
 * Maps to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found with identifier: " + identifier);
    }
}
