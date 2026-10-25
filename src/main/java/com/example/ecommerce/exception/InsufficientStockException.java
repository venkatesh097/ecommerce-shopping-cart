package com.example.ecommerce.exception;

/**
 * Exception thrown when requested product quantity exceeds available inventory.
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
