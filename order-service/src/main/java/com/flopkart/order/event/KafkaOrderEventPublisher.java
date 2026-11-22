package com.flopkart.order.event;

import com.flopkart.common.event.OrderPlacedEvent;
import com.flopkart.common.event.StockReleaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaOrderEventPublisher implements OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaOrderEventPublisher.class);

    private static final String ORDER_PLACED_TOPIC = "order-placed-topic";
    private static final String STOCK_RELEASE_TOPIC = "stock-release-topic";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaOrderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOrderPlaced(OrderPlacedEvent event) {
        try {
            kafkaTemplate.send(ORDER_PLACED_TOPIC, event.orderNumber(), event);
            log.info("Published OrderPlacedEvent to {} for order {}", ORDER_PLACED_TOPIC, event.orderNumber());
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent for order {}: {}", event.orderNumber(), e.getMessage());
        }
    }

    @Override
    public void publishStockRelease(StockReleaseEvent event) {
        try {
            kafkaTemplate.send(STOCK_RELEASE_TOPIC, event.orderNumber(), event);
            log.info("Published StockReleaseEvent to {} for order {}", STOCK_RELEASE_TOPIC, event.orderNumber());
        } catch (Exception e) {
            log.error("Failed to publish StockReleaseEvent for order {}: {}", event.orderNumber(), e.getMessage());
        }
    }
}
