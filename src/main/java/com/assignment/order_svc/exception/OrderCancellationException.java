package com.assignment.order_svc.exception;

/**
 * Exception thrown when attempting to cancel an order that cannot be cancelled.
 * Orders can only be cancelled when they are in PENDING status.
 * This is a runtime exception that indicates a business rule violation.
 */
public class OrderCancellationException extends RuntimeException {
    
    /**
     * Constructs a new OrderCancellationException with the specified detail message.
     * 
     * @param message the detail message explaining why the order cannot be cancelled
     */
    public OrderCancellationException(String message) {
        super(message);
    }
}

