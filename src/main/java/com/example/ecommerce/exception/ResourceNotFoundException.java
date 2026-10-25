package com.example.ecommerce.exception;

/**
 * Exception thrown when a requested domain resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
