package com.assignment.order_svc.model;

/**
 * Enum representing the possible states of an order in the system.
 * Orders follow a linear progression: PENDING → PROCESSING → SHIPPED → DELIVERED
 */
public enum OrderStatus {
    /**
     * Order has been created and is awaiting processing
     */
    PENDING,
    
    /**
     * Order is being prepared/processed
     */
    PROCESSING,
    
    /**
     * Order has been shipped to the customer
     */
    SHIPPED,
    
    /**
     * Order has been successfully delivered (terminal state)
     */
    DELIVERED;
    
    /**
     * Validates if a transition from the current status to a new status is allowed.
     * Valid transitions follow a linear progression:
     * PENDING → PROCESSING → SHIPPED → DELIVERED
     * 
     * @param newStatus the target status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        if (newStatus == null) {
            return false;
        }
        
        // Cannot transition to the same status
        if (this == newStatus) {
            return false;
        }
        
        // Define valid transitions
        switch (this) {
            case PENDING:
                return newStatus == PROCESSING;
            case PROCESSING:
                return newStatus == SHIPPED;
            case SHIPPED:
                return newStatus == DELIVERED;
            case DELIVERED:
                // DELIVERED is a terminal state - no further transitions allowed
                return false;
            default:
                return false;
        }
    }
}

