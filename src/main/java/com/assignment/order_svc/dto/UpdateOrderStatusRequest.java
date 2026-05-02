package com.assignment.order_svc.dto;

import com.assignment.order_svc.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating an order status.
 */
public class UpdateOrderStatusRequest {

    @NotNull
    private OrderStatus status;

    public UpdateOrderStatusRequest() {
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}

