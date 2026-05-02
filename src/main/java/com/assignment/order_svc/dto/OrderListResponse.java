package com.assignment.order_svc.dto;

import com.assignment.order_svc.model.Order;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Response DTO for paginated order list with custom structure.
 */
public class OrderListResponse {

    private List<OrderResponse> data;
    
    private PaginationResponse pagination;

    /**
     * Creates an OrderListResponse from a Spring Data Page of Orders.
     *
     * @param page the Spring Data Page containing orders
     * @return an OrderListResponse with transformed data and pagination metadata
     */
    public static OrderListResponse fromPage(Page<Order> page) {
        OrderListResponse response = new OrderListResponse();
        response.setData(page.getContent().stream()
                .map(OrderResponse::fromEntity)
                .toList());
        response.setPagination(PaginationResponse.fromPage(page));
        return response;
    }

    public List<OrderResponse> getData() {
        return data;
    }

    public void setData(List<OrderResponse> data) {
        this.data = data;
    }

    public PaginationResponse getPagination() {
        return pagination;
    }

    public void setPagination(PaginationResponse pagination) {
        this.pagination = pagination;
    }
}

