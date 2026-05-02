package com.assignment.order_svc.repository;

import com.assignment.order_svc.model.Order;
import com.assignment.order_svc.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity.
 * Provides data access operations for orders including custom query methods
 * for filtering by status and eagerly loading order items.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Finds orders by status with pagination support.
     * Used by the REST API to retrieve orders filtered by status.
     *
     * @param status the order status to filter by
     * @param pageable pagination information
     * @return a page of orders with the specified status
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * Finds all orders with a specific status.
     * Used by background jobs that need to process all orders in a particular state
     * (e.g., auto-canceling pending orders after timeout).
     *
     * @param status the order status to filter by
     * @return list of all orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Finds an order by ID with its items eagerly loaded.
     * Uses JOIN FETCH to prevent N+1 query problem by loading the order
     * and all its items in a single query.
     *
     * @param id the order ID
     * @return an Optional containing the order with items if found, empty otherwise
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}

