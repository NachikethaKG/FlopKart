package com.flopkart.payment.consumer;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrderConsumer.class);

    private final PaymentService paymentService;

    public PaymentOrderConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "order-placed-topic",
            groupId = "${spring.kafka.consumer.group-id:payment-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderPlacedEvent(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent from order-placed-topic: {}", event);
        paymentService.processOrderPayment(event);
    }
}
