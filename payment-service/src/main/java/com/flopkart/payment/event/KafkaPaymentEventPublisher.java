package com.flopkart.payment.event;

import com.flopkart.common.event.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentEventPublisher implements PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaPaymentEventPublisher.class);
    private static final String PAYMENT_STATUS_TOPIC = "payment-status-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        try {
            kafkaTemplate.send(PAYMENT_STATUS_TOPIC, event.orderNumber(), event);
            log.info("Published PaymentProcessedEvent to {} for order {}: status={}",
                    PAYMENT_STATUS_TOPIC, event.orderNumber(), event.status());
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent for order {}: {}", event.orderNumber(), e.getMessage());
        }
    }
}
