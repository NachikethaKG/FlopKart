package com.flopkart.product.consumer;

import com.flopkart.common.dto.product.StockItemDto;
import com.flopkart.common.dto.product.StockReleaseRequest;
import com.flopkart.common.event.StockReleaseEvent;
import com.flopkart.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockReleaseConsumer {

    private static final Logger log = LoggerFactory.getLogger(StockReleaseConsumer.class);

    private final ProductService productService;

    public StockReleaseConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(
            topics = "stock-release-topic",
            groupId = "${spring.kafka.consumer.group-id:product-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStockReleaseEvent(StockReleaseEvent event) {
        if (event == null || event.items() == null || event.items().isEmpty()) {
            log.warn("Received empty StockReleaseEvent, skipping");
            return;
        }

        log.info("Received StockReleaseEvent for order {}: restoring {} items",
                event.orderNumber(), event.items().size());

        List<StockItemDto> items = event.items().stream()
                .map(item -> new StockItemDto(item.productId(), item.productName(), item.quantity()))
                .toList();

        StockReleaseRequest request = new StockReleaseRequest(event.orderNumber(), items);
        productService.releaseStock(request);
        log.info("Successfully restored inventory for order {}", event.orderNumber());
    }
}
