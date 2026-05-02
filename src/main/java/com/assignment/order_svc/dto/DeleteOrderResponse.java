package com.assignment.order_svc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Response DTO for order deletion operations.
 */
public class DeleteOrderResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deletedAt;

    /**
     * Default constructor for Jackson deserialization.
     */
    public DeleteOrderResponse() {
    }

    /**
     * Creates a delete order response with the specified details.
     *
     * @param id the ID of the deleted order
     * @param deletedAt the timestamp when the order was deleted
     */
    public DeleteOrderResponse(Long id, LocalDateTime deletedAt) {
        this.id = id;
        this.deletedAt = deletedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}

