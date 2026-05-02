package com.assignment.order_svc.exception;

/**
 * Exception thrown when attempting an invalid order status transition.
 * For example, trying to transition from DELIVERED to PENDING, or from PROCESSING to PENDING.
 * This is a runtime exception that indicates a business rule violation.
 */
public class InvalidOrderStatusTransitionException extends RuntimeException {
    
    /**
     * Constructs a new InvalidOrderStatusTransitionException with the specified detail message.
     * 
     * @param message the detail message explaining why the status transition is invalid
     */
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}

