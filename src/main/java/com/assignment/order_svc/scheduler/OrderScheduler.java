package com.assignment.order_svc.scheduler;

import com.assignment.order_svc.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Background scheduler for processing pending orders.
 * This scheduler runs periodically to transition orders from PENDING to PROCESSING status.
 * 
 * <p>Execution frequency: Every 5 minutes (300,000 milliseconds)</p>
 * 
 * <p>The scheduler ensures that pending orders are automatically processed without manual intervention,
 * providing a seamless order fulfillment workflow.</p>
 */
@Component
public class OrderScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderScheduler.class);
    
    private final OrderService orderService;
    
    /**
     * Constructs the OrderScheduler with required dependencies.
     * 
     * @param orderService the order service for processing pending orders
     */
    public OrderScheduler(OrderService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Scheduled job that processes all pending orders.
     * Runs every 5 minutes (300,000 milliseconds) to transition orders from PENDING to PROCESSING status.
     * 
     * <p>This method handles exceptions gracefully to ensure the scheduler continues running
     * even if a single execution fails.</p>
     */
    @Scheduled(fixedRate = 300000)
    public void processOrders() {
        try {
            logger.info("Starting scheduled job: Processing pending orders");
            orderService.processPendingOrders();
            logger.info("Completed scheduled job: Pending orders processed successfully");
        } catch (Exception e) {
            logger.error("Error occurred while processing pending orders in scheduled job", e);
        }
    }
}

