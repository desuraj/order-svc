package com.assignment.order_svc.service;

import com.assignment.order_svc.model.Order;
import com.assignment.order_svc.model.OrderItem;
import com.assignment.order_svc.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface defining business operations for order management.
 * Provides methods for creating, retrieving, updating, and cancelling orders,
 * as well as background processing operations.
 */
public interface OrderService {
    
    /**
     * Creates a new order with the specified customer ID and items.
     * The order is created with PENDING status and the total amount is calculated
     * from the items.
     * 
     * @param customerId the customer identifier
     * @param items the list of order items
     * @return the created order with generated ID
     * @throws IllegalArgumentException if items list is empty or null
     */
    Order createOrder(String customerId, List<OrderItem> items);
    
    /**
     * Retrieves an order by its ID with all items eagerly loaded.
     * 
     * @param id the order ID
     * @return the order with the specified ID
     * @throws com.assignment.order_svc.exception.OrderNotFoundException if order not found
     */
    Order getOrderById(Long id);
    
    /**
     * Updates the status of an order after validating the status transition.
     * Only valid status transitions are allowed (PENDING → PROCESSING → SHIPPED → DELIVERED).
     * 
     * @param id the order ID
     * @param newStatus the new status to transition to
     * @return the updated order
     * @throws com.assignment.order_svc.exception.OrderNotFoundException if order not found
     * @throws com.assignment.order_svc.exception.InvalidOrderStatusTransitionException if transition is invalid
     */
    Order updateOrderStatus(Long id, OrderStatus newStatus);
    
    /**
     * Lists orders with optional status filter and pagination support.
     * If status is null, returns all orders paginated.
     * 
     * @param status the order status to filter by (null for all orders)
     * @param pageable pagination information
     * @return a page of orders matching the criteria
     */
    Page<Order> listOrders(OrderStatus status, Pageable pageable);
    
    /**
     * Cancels an order by deleting it from the system.
     * Only orders in PENDING status can be cancelled.
     * 
     * @param id the order ID
     * @throws com.assignment.order_svc.exception.OrderNotFoundException if order not found
     * @throws com.assignment.order_svc.exception.OrderCancellationException if order is not in PENDING status
     */
    void cancelOrder(Long id);
    
    /**
     * Background job method to process all pending orders.
     * Transitions all orders with PENDING status to PROCESSING status.
     * This method is typically called by a scheduled task.
     */
    void processPendingOrders();
}

