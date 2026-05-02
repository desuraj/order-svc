package com.assignment.order_svc.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an order in the e-commerce system.
 * An Order contains multiple OrderItems and tracks the order lifecycle through status transitions.
 * Uses optimistic locking to handle concurrent updates.
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_status", columnList = "status"),
    @Index(name = "idx_orders_customer_id", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "items")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;
    
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Version
    private Long version;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Constructor for creating an Order with required fields
     *
     * @param customerId the customer identifier
     */
    public Order(String customerId) {
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.totalAmount = BigDecimal.ZERO;
    }
    
    /**
     * Lifecycle callback to set createdAt timestamp before persisting
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Lifecycle callback to update updatedAt timestamp before updating
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculates the total amount by summing up all item prices multiplied by their quantities.
     * 
     * @return the calculated total amount
     */
    public BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Validates if a transition to a new status is allowed based on the current status.
     * Delegates to the OrderStatus enum's canTransitionTo method.
     * 
     * @param newStatus the target status to transition to
     * @return true if the transition is valid, false otherwise
     */
    public boolean canTransitionTo(OrderStatus newStatus) {
        return this.status.canTransitionTo(newStatus);
    }
    
    /**
     * Helper method to add an item to the order while maintaining bidirectional relationship.
     * 
     * @param item the OrderItem to add
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    
    /**
     * Helper method to remove an item from the order while maintaining bidirectional relationship.
     * 
     * @param item the OrderItem to remove
     */
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
}

