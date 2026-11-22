package com.flopkart.order.consumer;

import com.flopkart.common.event.PaymentProcessedEvent;
import com.flopkart.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPaymentConsumer.class);

    private final OrderService orderService;

    public OrderPaymentConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(
            topics = "payment-status-topic",
            groupId = "${spring.kafka.consumer.group-id:order-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("Received PaymentProcessedEvent from payment-status-topic: {}", event);
        orderService.processPaymentResult(event);
    }
}
