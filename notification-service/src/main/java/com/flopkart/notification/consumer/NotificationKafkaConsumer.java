package com.flopkart.notification.consumer;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.notification.service.NotificationDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    private final NotificationDispatcher notificationDispatcher;

    public NotificationKafkaConsumer(NotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    @KafkaListener(
            topics = "order-placed-topic",
            groupId = "${spring.kafka.consumer.group-id:notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Notification Service received OrderPlacedEvent for order: {}", event.orderNumber());
        notificationDispatcher.dispatchOrderPlacedNotification(event);
    }

    @KafkaListener(
            topics = "payment-status-topic",
            groupId = "${spring.kafka.consumer.group-id:notification-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Notification Service received PaymentProcessedEvent for order {}: status={}",
                event.orderNumber(), event.status());

        if ("SUCCESS".equalsIgnoreCase(event.status())) {
            notificationDispatcher.dispatchPaymentSuccessNotification(event);
        } else {
            notificationDispatcher.dispatchPaymentFailedNotification(event);
        }
    }
}
