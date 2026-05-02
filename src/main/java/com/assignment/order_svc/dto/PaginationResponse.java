package com.assignment.order_svc.dto;

import org.springframework.data.domain.Page;

/**
 * Response DTO for pagination metadata.
 */
public class PaginationResponse {

    private int page;
    
    private int size;
    
    private long totalElements;
    
    private int totalPages;
    
    private boolean hasNext;
    
    private boolean hasPrevious;

    /**
     * Creates a PaginationResponse from a Spring Data Page object.
     *
     * @param page the Spring Data Page object
     * @param <T> the type of content in the page
     * @return a PaginationResponse with pagination metadata
     */
    public static <T> PaginationResponse fromPage(Page<T> page) {
        PaginationResponse response = new PaginationResponse();
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}

