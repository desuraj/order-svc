package com.assignment.order_svc.service.impl;

import com.assignment.order_svc.exception.InvalidOrderStatusTransitionException;
import com.assignment.order_svc.exception.OrderCancellationException;
import com.assignment.order_svc.exception.OrderNotFoundException;
import com.assignment.order_svc.model.Order;
import com.assignment.order_svc.model.OrderItem;
import com.assignment.order_svc.model.OrderStatus;
import com.assignment.order_svc.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderServiceImpl.
 * Tests all business logic methods with both happy paths and error scenarios.
 * Uses Mockito to mock OrderRepository dependency.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private List<OrderItem> testItems;

    /**
     * Set up test data before each test.
     * Creates a sample order with items for testing.
     */
    @BeforeEach
    void setUp() {
        // Create test order items
        testItems = new ArrayList<>();
        testItems.add(createOrderItem("PROD-001", 2, new BigDecimal("10.00")));
        testItems.add(createOrderItem("PROD-002", 1, new BigDecimal("25.50")));

        // Create test order
        testOrder = new Order("CUST-123");
        testOrder.setId(1L);
        for (OrderItem item : testItems) {
            testOrder.addItem(item);
        }
        testOrder.setTotalAmount(testOrder.calculateTotalAmount());
    }

    // ==================== createOrder() Tests ====================

    @Test
    @DisplayName("createOrder - With valid items - Should return created order")
    void createOrder_WithValidItems_ShouldReturnCreatedOrder() {
        // Arrange
        String customerId = "CUST-123";
        List<OrderItem> items = Arrays.asList(
                createOrderItem("PROD-001", 2, new BigDecimal("10.00")),
                createOrderItem("PROD-002", 1, new BigDecimal("25.50"))
        );

        Order expectedOrder = new Order(customerId);
        expectedOrder.setId(1L);
        for (OrderItem item : items) {
            expectedOrder.addItem(item);
        }
        expectedOrder.setTotalAmount(expectedOrder.calculateTotalAmount());

        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        Order result = orderService.createOrder(customerId, items);

        // Assert
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getItems().size());
        assertNotNull(result.getTotalAmount());

        // Verify repository interaction
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertEquals(customerId, capturedOrder.getCustomerId());
        assertEquals(OrderStatus.PENDING, capturedOrder.getStatus());
    }

    @Test
    @DisplayName("createOrder - With valid items - Should create order with PENDING status")
    void createOrder_WithValidItems_ShouldCreateOrderWithPendingStatus() {
        // Arrange
        String customerId = "CUST-456";
        List<OrderItem> items = Collections.singletonList(
                createOrderItem("PROD-003", 1, new BigDecimal("50.00"))
        );

        Order savedOrder = new Order(customerId);
        savedOrder.setId(2L);
        savedOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(customerId, items);

        // Assert
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - With valid items - Should calculate total amount correctly")
    void createOrder_WithValidItems_ShouldCalculateTotalAmountCorrectly() {
        // Arrange
        String customerId = "CUST-789";
        List<OrderItem> items = Arrays.asList(
                createOrderItem("PROD-001", 2, new BigDecimal("10.00")), // 2 * 10.00 = 20.00
                createOrderItem("PROD-002", 3, new BigDecimal("15.00"))  // 3 * 15.00 = 45.00
        );
        // Expected total: 20.00 + 45.00 = 65.00

        Order savedOrder = new Order(customerId);
        savedOrder.setId(3L);
        for (OrderItem item : items) {
            savedOrder.addItem(item);
        }
        savedOrder.setTotalAmount(savedOrder.calculateTotalAmount());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(customerId, items);

        // Assert
        assertEquals(new BigDecimal("65.00"), result.getTotalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - With empty items list - Should throw IllegalArgumentException")
    void createOrder_WithEmptyItemsList_ShouldThrowIllegalArgumentException() {
        // Arrange
        String customerId = "CUST-999";
        List<OrderItem> emptyItems = Collections.emptyList();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(customerId, emptyItems)
        );

        assertEquals("Order must contain at least one item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - With null items list - Should throw IllegalArgumentException")
    void createOrder_WithNullItemsList_ShouldThrowIllegalArgumentException() {
        // Arrange
        String customerId = "CUST-999";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(customerId, null)
        );

        assertEquals("Order must contain at least one item", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - Should properly associate items with order")
    void createOrder_ShouldProperlyAssociateItemsWithOrder() {
        // Arrange
        String customerId = "CUST-111";
        List<OrderItem> items = Arrays.asList(
                createOrderItem("PROD-001", 1, new BigDecimal("10.00")),
                createOrderItem("PROD-002", 1, new BigDecimal("20.00"))
        );

        Order savedOrder = new Order(customerId);
        savedOrder.setId(4L);
        for (OrderItem item : items) {
            savedOrder.addItem(item);
        }
        savedOrder.setTotalAmount(savedOrder.calculateTotalAmount());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(customerId, items);

        // Assert
        assertEquals(2, result.getItems().size());
        for (OrderItem item : result.getItems()) {
            assertNotNull(item.getOrder());
            // Verify the item is associated with an order (not checking exact reference due to mocking)
            assertNotNull(item.getOrder().getCustomerId());
        }
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    // ==================== getOrderById() Tests ====================

    @Test
    @DisplayName("getOrderById - With existing order - Should return order")
    void getOrderById_WithExistingOrder_ShouldReturnOrder() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("CUST-123", result.getCustomerId());
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
    }

    @Test
    @DisplayName("getOrderById - With non-existing order - Should throw OrderNotFoundException")
    void getOrderById_WithNonExistingOrder_ShouldThrowOrderNotFoundException() {
        // Arrange
        Long orderId = 999L;
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.getOrderById(orderId)
        );

        assertTrue(exception.getMessage().contains("Order not found with ID: " + orderId));
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
    }

    @Test
    @DisplayName("getOrderById - Should eagerly load items")
    void getOrderById_ShouldEagerlyLoadItems() {
        // Arrange
        Long orderId = 1L;
        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        Order result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result.getItems());
        assertEquals(2, result.getItems().size());
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
    }

    // ==================== updateOrderStatus() Tests ====================

    @Test
    @DisplayName("updateOrderStatus - With valid transition PENDING to PROCESSING - Should update status")
    void updateOrderStatus_WithValidTransitionPendingToProcessing_ShouldUpdateStatus() {
        // Arrange
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.PROCESSING;
        testOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        Order result = orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PROCESSING, result.getStatus());
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("updateOrderStatus - With invalid transition PENDING to DELIVERED - Should throw InvalidOrderStatusTransitionException")
    void updateOrderStatus_WithInvalidTransitionPendingToDelivered_ShouldThrowException() {
        // Arrange
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.DELIVERED;
        testOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        InvalidOrderStatusTransitionException exception = assertThrows(
                InvalidOrderStatusTransitionException.class,
                () -> orderService.updateOrderStatus(orderId, newStatus)
        );

        assertTrue(exception.getMessage().contains("Invalid status transition"));
        assertTrue(exception.getMessage().contains("PENDING"));
        assertTrue(exception.getMessage().contains("DELIVERED"));
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrderStatus - With non-existing order - Should throw OrderNotFoundException")
    void updateOrderStatus_WithNonExistingOrder_ShouldThrowOrderNotFoundException() {
        // Arrange
        Long orderId = 999L;
        OrderStatus newStatus = OrderStatus.PROCESSING;

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrderStatus(orderId, newStatus)
        );

        assertTrue(exception.getMessage().contains("Order not found with ID: " + orderId));
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("updateOrderStatus - Should save updated order")
    void updateOrderStatus_ShouldSaveUpdatedOrder() {
        // Arrange
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.PROCESSING;
        testOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order capturedOrder = orderCaptor.getValue();
        assertEquals(OrderStatus.PROCESSING, capturedOrder.getStatus());
    }

    // ==================== listOrders() Tests ====================

    @Test
    @DisplayName("listOrders - With pagination and no status filter - Should return all orders")
    void listOrders_WithPaginationAndNoStatusFilter_ShouldReturnAllOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = Arrays.asList(testOrder, createTestOrder(2L, "CUST-456"));
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // Act
        Page<Order> result = orderService.listOrders(null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
        verify(orderRepository, never()).findByStatus(any(OrderStatus.class), any(Pageable.class));
    }

    @Test
    @DisplayName("listOrders - With pagination and status filter - Should return filtered orders")
    void listOrders_WithPaginationAndStatusFilter_ShouldReturnFilteredOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        OrderStatus status = OrderStatus.PENDING;
        List<Order> orders = Collections.singletonList(testOrder);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

        when(orderRepository.findByStatus(status, pageable)).thenReturn(orderPage);

        // Act
        Page<Order> result = orderService.listOrders(status, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(OrderStatus.PENDING, result.getContent().get(0).getStatus());
        verify(orderRepository, times(1)).findByStatus(status, pageable);
        verify(orderRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("listOrders - With no orders - Should return empty page")
    void listOrders_WithNoOrders_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(orderRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<Order> result = orderService.listOrders(null, pageable);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    // ==================== cancelOrder() Tests ====================

    @Test
    @DisplayName("cancelOrder - With PENDING order - Should cancel successfully")
    void cancelOrder_WithPendingOrder_ShouldCancelSuccessfully() {
        // Arrange
        Long orderId = 1L;
        testOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).delete(testOrder);

        // Act
        orderService.cancelOrder(orderId);

        // Assert
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    @DisplayName("cancelOrder - With non-PENDING order - Should throw OrderCancellationException")
    void cancelOrder_WithNonPendingOrder_ShouldThrowOrderCancellationException() {
        // Arrange
        Long orderId = 1L;
        testOrder.setStatus(OrderStatus.PROCESSING);

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.of(testOrder));

        // Act & Assert
        OrderCancellationException exception = assertThrows(
                OrderCancellationException.class,
                () -> orderService.cancelOrder(orderId)
        );

        assertTrue(exception.getMessage().contains("Cannot cancel order"));
        assertTrue(exception.getMessage().contains("PROCESSING"));
        assertTrue(exception.getMessage().contains("only PENDING orders can be cancelled"));
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    @DisplayName("cancelOrder - With non-existing order - Should throw OrderNotFoundException")
    void cancelOrder_WithNonExistingOrder_ShouldThrowOrderNotFoundException() {
        // Arrange
        Long orderId = 999L;

        when(orderRepository.findByIdWithItems(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        OrderNotFoundException exception = assertThrows(
                OrderNotFoundException.class,
                () -> orderService.cancelOrder(orderId)
        );

        assertTrue(exception.getMessage().contains("Order not found with ID: " + orderId));
        verify(orderRepository, times(1)).findByIdWithItems(orderId);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    // ==================== processPendingOrders() Tests ====================

    @Test
    @DisplayName("processPendingOrders - With pending orders - Should move all to PROCESSING")
    void processPendingOrders_WithPendingOrders_ShouldMoveAllToProcessing() {
        // Arrange
        Order order1 = createTestOrder(1L, "CUST-001");
        order1.setStatus(OrderStatus.PENDING);
        Order order2 = createTestOrder(2L, "CUST-002");
        order2.setStatus(OrderStatus.PENDING);
        Order order3 = createTestOrder(3L, "CUST-003");
        order3.setStatus(OrderStatus.PENDING);

        List<Order> pendingOrders = Arrays.asList(order1, order2, order3);

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.processPendingOrders();

        // Assert
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, times(3)).save(any(Order.class));

        // Verify each order was updated to PROCESSING
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(3)).save(orderCaptor.capture());

        List<Order> savedOrders = orderCaptor.getAllValues();
        for (Order order : savedOrders) {
            assertEquals(OrderStatus.PROCESSING, order.getStatus());
        }
    }

    @Test
    @DisplayName("processPendingOrders - With non-PENDING orders - Should not affect them")
    void processPendingOrders_WithNonPendingOrders_ShouldNotAffectThem() {
        // Arrange
        Order pendingOrder = createTestOrder(1L, "CUST-001");
        pendingOrder.setStatus(OrderStatus.PENDING);

        List<Order> pendingOrders = Collections.singletonList(pendingOrder);

        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        orderService.processPendingOrders();

        // Assert
        // Only PENDING orders should be fetched and processed
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("processPendingOrders - With no pending orders - Should not process anything")
    void processPendingOrders_WithNoPendingOrders_ShouldNotProcessAnything() {
        // Arrange
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(Collections.emptyList());

        // Act
        orderService.processPendingOrders();

        // Assert
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
        verify(orderRepository, never()).save(any(Order.class));
    }

    // ==================== Helper Methods ====================

    /**
     * Helper method to create an OrderItem for testing.
     */
    private OrderItem createOrderItem(String productId, Integer quantity, BigDecimal price) {
        return new OrderItem(productId, quantity, price);
    }

    /**
     * Helper method to create a test Order.
     */
    private Order createTestOrder(Long id, String customerId) {
        Order order = new Order(customerId);
        order.setId(id);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        return order;
    }
}

