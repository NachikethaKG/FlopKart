package com.flopkart.notification.service;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingNotificationDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationDispatcher.class);

    @Override
    public void dispatchOrderPlacedNotification(OrderPlacedEvent event) {
        StringBuilder itemsSummary = new StringBuilder();
        if (event.items() != null) {
            for (var item : event.items()) {
                itemsSummary.append(String.format("\n  - %dx %s @ $%s (Subtotal: $%s)",
                        item.quantity(), item.productName(), item.price(), item.subtotal()));
            }
        }

        String receipt = String.format("""
                
                ================================================================================
                [EMAIL/SMS RECEIPT] ORDER CREATION CONFIRMATION
                To: %s (User ID: %s)
                Order Number: %s
                Total Amount: $%s
                Items:%s
                Status: PENDING PAYMENT
                Timestamp: %s
                ================================================================================""",
                event.userEmail(),
                event.userId(),
                event.orderNumber(),
                event.totalAmount(),
                itemsSummary,
                event.timestamp()
        );

        log.info(receipt);
    }

    @Override
    public void dispatchPaymentSuccessNotification(PaymentProcessedEvent event) {
        String receipt = String.format("""
                
                ================================================================================
                [EMAIL/SMS RECEIPT] PAYMENT SUCCESS & ORDER CONFIRMED
                Order Number: %s
                Payment Reference: %s
                Status: CONFIRMED - Transaction Approved
                Timestamp: %s
                ================================================================================""",
                event.orderNumber(),
                event.paymentId(),
                event.timestamp()
        );

        log.info(receipt);
    }

    @Override
    public void dispatchPaymentFailedNotification(PaymentProcessedEvent event) {
        String receipt = String.format("""
                
                ================================================================================
                [EMAIL/SMS NOTICE] PAYMENT FAILED & ORDER CANCELLED
                Order Number: %s
                Payment Reference: %s
                Status: CANCELLED
                Failure Reason: %s
                Compensating Action: Reserved inventory has been automatically restored.
                Timestamp: %s
                ================================================================================""",
                event.orderNumber(),
                event.paymentId(),
                event.failureReason(),
                event.timestamp()
        );

        log.warn(receipt);
    }
}
