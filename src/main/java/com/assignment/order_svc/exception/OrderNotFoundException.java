package com.assignment.order_svc.exception;

/**
 * Exception thrown when an order with the specified ID cannot be found in the system.
 * This is a runtime exception that indicates a client error (404 Not Found scenario).
 */
public class OrderNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new OrderNotFoundException with the specified detail message.
     * 
     * @param message the detail message explaining why the order was not found
     */
    public OrderNotFoundException(String message) {
        super(message);
    }
}

