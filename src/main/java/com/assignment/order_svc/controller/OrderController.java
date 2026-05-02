package com.assignment.order_svc.controller;

import com.assignment.order_svc.dto.CreateOrderRequest;
import com.assignment.order_svc.dto.DeleteOrderResponse;
import com.assignment.order_svc.dto.OrderListResponse;
import com.assignment.order_svc.dto.OrderResponse;
import com.assignment.order_svc.dto.UpdateOrderStatusRequest;
import com.assignment.order_svc.model.Order;
import com.assignment.order_svc.model.OrderItem;
import com.assignment.order_svc.model.OrderStatus;
import com.assignment.order_svc.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller exposing order management endpoints.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * Creates a controller with the required service dependency.
     *
     * @param orderService the order service
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order from the provided customer and item details.
     *
     * @param request the create order request payload
     * @return the created order response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream()
                .map(item -> new OrderItem(item.getProductId(), item.getQuantity(), item.getPrice()))
                .toList();

        Order createdOrder = orderService.createOrder(request.getCustomerId(), items);
        return OrderResponse.fromEntity(createdOrder);
    }

    /**
     * Retrieves an order by its identifier.
     *
     * @param id the order identifier
     * @return the matching order response
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse getOrderById(@PathVariable Long id) {
        return OrderResponse.fromEntity(orderService.getOrderById(id));
    }

    /**
     * Updates the status of an existing order.
     *
     * @param id the order identifier
     * @param request the status update request payload
     * @return the updated order response
     */
    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public OrderResponse updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return OrderResponse.fromEntity(orderService.updateOrderStatus(id, request.getStatus()));
    }

    /**
     * Lists orders with optional status filtering and pagination.
     *
     * @param status optional order status filter
     * @param pageable pagination and sorting settings
     * @return a custom order list response with data and pagination metadata
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public OrderListResponse listOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Order> orderPage = orderService.listOrders(status, pageable);
        return OrderListResponse.fromPage(orderPage);
    }

    /**
     * Cancels an order by deleting it when cancellation rules allow it.
     *
     * @param id the order identifier
     * @return the delete order response with deleted order ID and timestamp
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public DeleteOrderResponse cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return new DeleteOrderResponse(id, LocalDateTime.now());
    }
}

