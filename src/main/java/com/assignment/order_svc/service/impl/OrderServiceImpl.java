package com.assignment.order_svc.service.impl;

import com.assignment.order_svc.exception.InvalidOrderStatusTransitionException;
import com.assignment.order_svc.exception.OrderCancellationException;
import com.assignment.order_svc.exception.OrderNotFoundException;
import com.assignment.order_svc.model.Order;
import com.assignment.order_svc.model.OrderItem;
import com.assignment.order_svc.model.OrderStatus;
import com.assignment.order_svc.repository.OrderRepository;
import com.assignment.order_svc.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of OrderService interface providing business logic for order management.
 * Handles order creation, retrieval, status updates, cancellation, and background processing.
 * Uses constructor-based dependency injection and transactional operations for data consistency.
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final OrderRepository orderRepository;
    
    /**
     * Constructor for dependency injection.
     * 
     * @param orderRepository the order repository
     */
    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    
    /**
     * Creates a new order with the specified customer ID and items.
     * Validates that items list is not empty, creates order with PENDING status,
     * establishes bidirectional relationships, calculates total amount, and persists.
     * 
     * @param customerId the customer identifier
     * @param items the list of order items
     * @return the created order with generated ID
     * @throws IllegalArgumentException if items list is empty or null
     */
    @Override
    @Transactional
    public Order createOrder(String customerId, List<OrderItem> items) {
        logger.info("Creating order for customer: {}", customerId);
        
        // Validate items
        if (items == null || items.isEmpty()) {
            logger.error("Cannot create order with empty items list for customer: {}", customerId);
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        
        // Create order with PENDING status
        Order order = new Order(customerId);
        
        // Add items with bidirectional relationship
        for (OrderItem item : items) {
            order.addItem(item);
        }
        
        // Calculate and set total amount
        BigDecimal totalAmount = order.calculateTotalAmount();
        order.setTotalAmount(totalAmount);
        
        // Save order
        Order savedOrder = orderRepository.save(order);
        
        logger.info("Order created successfully with ID: {} for customer: {}, total amount: {}", 
                    savedOrder.getId(), customerId, totalAmount);
        
        return savedOrder;
    }
    
    /**
     * Retrieves an order by its ID with all items eagerly loaded.
     * Uses findByIdWithItems to prevent N+1 query problem.
     * 
     * @param id the order ID
     * @return the order with the specified ID
     * @throws OrderNotFoundException if order not found
     */
    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        logger.debug("Fetching order with ID: {}", id);
        
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", id);
                    return new OrderNotFoundException("Order not found with ID: " + id);
                });
    }
    
    /**
     * Updates the status of an order after validating the status transition.
     * Validates transition rules, updates status, and handles optimistic locking.
     * 
     * @param id the order ID
     * @param newStatus the new status to transition to
     * @return the updated order
     * @throws OrderNotFoundException if order not found
     * @throws InvalidOrderStatusTransitionException if transition is invalid
     */
    @Override
    @Transactional
    public Order updateOrderStatus(Long id, OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", id, newStatus);
        
        // Find order
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", id);
                    return new OrderNotFoundException("Order not found with ID: " + id);
                });
        
        // Validate status transition
        if (!order.canTransitionTo(newStatus)) {
            String errorMsg = String.format("Invalid status transition from %s to %s for order ID: %d",
                    order.getStatus(), newStatus, id);
            logger.error(errorMsg);
            throw new InvalidOrderStatusTransitionException(errorMsg);
        }
        
        // Update status
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        try {
            // Save with optimistic locking
            Order updatedOrder = orderRepository.save(order);
            logger.info("Order {} status updated from {} to {}", id, oldStatus, newStatus);
            return updatedOrder;
        } catch (ObjectOptimisticLockingFailureException e) {
            logger.error("Optimistic locking failure while updating order {}: {}", id, e.getMessage());
            throw new InvalidOrderStatusTransitionException(
                    "Order was modified by another transaction. Please retry.");
        }
    }
    
    /**
     * Lists orders with optional status filter and pagination support.
     * If status is null, returns all orders paginated.
     * 
     * @param status the order status to filter by (null for all orders)
     * @param pageable pagination information
     * @return a page of orders matching the criteria
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Order> listOrders(OrderStatus status, Pageable pageable) {
        if (status == null) {
            logger.debug("Fetching all orders with pagination: page {}, size {}", 
                        pageable.getPageNumber(), pageable.getPageSize());
            return orderRepository.findAll(pageable);
        } else {
            logger.debug("Fetching orders with status {} and pagination: page {}, size {}", 
                        status, pageable.getPageNumber(), pageable.getPageSize());
            return orderRepository.findByStatus(status, pageable);
        }
    }
    
    /**
     * Cancels an order by deleting it from the system.
     * Only orders in PENDING status can be cancelled.
     * 
     * @param id the order ID
     * @throws OrderNotFoundException if order not found
     * @throws OrderCancellationException if order is not in PENDING status
     */
    @Override
    @Transactional
    public void cancelOrder(Long id) {
        logger.info("Attempting to cancel order with ID: {}", id);
        
        // Find order
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", id);
                    return new OrderNotFoundException("Order not found with ID: " + id);
                });
        
        // Check if order is in PENDING status
        if (order.getStatus() != OrderStatus.PENDING) {
            String errorMsg = String.format("Cannot cancel order with ID: %d. Order status is %s, only PENDING orders can be cancelled",
                    id, order.getStatus());
            logger.error(errorMsg);
            throw new OrderCancellationException(errorMsg);
        }
        
        // Delete order
        orderRepository.delete(order);
        logger.info("Order {} cancelled successfully", id);
    }
    
    /**
     * Background job method to process all pending orders.
     * Transitions all orders with PENDING status to PROCESSING status.
     * This method is typically called by a scheduled task.
     */
    @Override
    @Transactional
    public void processPendingOrders() {
        logger.info("Starting background job to process pending orders");
        
        // Find all PENDING orders
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        
        if (pendingOrders.isEmpty()) {
            logger.info("No pending orders to process");
            return;
        }
        
        logger.info("Found {} pending orders to process", pendingOrders.size());
        
        // Update each order to PROCESSING
        int processedCount = 0;
        for (Order order : pendingOrders) {
            try {
                order.setStatus(OrderStatus.PROCESSING);
                orderRepository.save(order);
                processedCount++;
                logger.debug("Order {} transitioned from PENDING to PROCESSING", order.getId());
            } catch (Exception e) {
                logger.error("Failed to process order {}: {}", order.getId(), e.getMessage());
                // Continue processing other orders
            }
        }
        
        logger.info("Background job completed. Processed {} out of {} pending orders", 
                    processedCount, pendingOrders.size());
    }
}

