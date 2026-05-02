package com.assignment.order_svc.repository;

import com.assignment.order_svc.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for OrderItem entity.
 * Provides standard CRUD operations for order items.
 * 
 * OrderItems are typically managed through the Order entity's cascade operations,
 * but this repository provides direct access if needed for specific use cases.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // No custom methods needed - standard CRUD operations are sufficient
}

