package com.flopkart.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class EventRecordsTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("OrderItemDto record creates subtotal and serializes correctly")
    void shouldSerializeOrderItemDto() throws Exception {
        OrderItemDto item = new OrderItemDto(10L, "Sneakers", 2, new BigDecimal("49.99"));
        assertEquals(new BigDecimal("99.98"), item.subtotal());

        String json = objectMapper.writeValueAsString(item);
        OrderItemDto deserialized = objectMapper.readValue(json, OrderItemDto.class);

        assertEquals(item.productId(), deserialized.productId());
        assertEquals(item.productName(), deserialized.productName());
        assertEquals(item.quantity(), deserialized.quantity());
        assertEquals(item.price(), deserialized.price());
    }

    @Test
    @DisplayName("OrderPlacedEvent records are immutable and serialize cleanly")
    void shouldSerializeOrderPlacedEvent() throws Exception {
        OrderItemDto item = new OrderItemDto(1L, "Phone", 1, new BigDecimal("699.99"));
        LocalDateTime now = LocalDateTime.now();
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-12345",
                "user-10",
                "user@example.com",
                new BigDecimal("699.99"),
                List.of(item),
                now
        );

        assertEquals("ORD-12345", event.orderNumber());
        assertEquals(1, event.items().size());

        String json = objectMapper.writeValueAsString(event);
        OrderPlacedEvent deserialized = objectMapper.readValue(json, OrderPlacedEvent.class);

        assertEquals("ORD-12345", deserialized.orderNumber());
        assertEquals("user-10", deserialized.userId());
        assertEquals(1, deserialized.items().size());
        assertEquals("Phone", deserialized.items().get(0).productName());
    }

    @Test
    @DisplayName("PaymentProcessedEvent correctly identifies success and failure states")
    void shouldHandlePaymentProcessedEvent() throws Exception {
        PaymentProcessedEvent success = new PaymentProcessedEvent(
                "ORD-100", "PAY-200", "SUCCESS", null, LocalDateTime.now()
        );
        assertTrue(success.isSuccessful());

        PaymentProcessedEvent failure = new PaymentProcessedEvent(
                "ORD-101", "PAY-201", "FAILED", "Insufficient Funds", LocalDateTime.now()
        );
        assertFalse(failure.isSuccessful());
        assertEquals("Insufficient Funds", failure.failureReason());

        String json = objectMapper.writeValueAsString(failure);
        PaymentProcessedEvent deserialized = objectMapper.readValue(json, PaymentProcessedEvent.class);
        assertEquals("FAILED", deserialized.status());
        assertEquals("Insufficient Funds", deserialized.failureReason());
    }

    @Test
    @DisplayName("StockReleaseEvent serializes compensating payload")
    void shouldSerializeStockReleaseEvent() throws Exception {
        OrderItemDto item = new OrderItemDto(5L, "Jacket", 2, new BigDecimal("120.00"));
        StockReleaseEvent event = new StockReleaseEvent("ORD-999", List.of(item), LocalDateTime.now());

        String json = objectMapper.writeValueAsString(event);
        StockReleaseEvent deserialized = objectMapper.readValue(json, StockReleaseEvent.class);

        assertEquals("ORD-999", deserialized.orderNumber());
        assertThat(deserialized.items()).hasSize(1);
        assertEquals(5L, deserialized.items().get(0).productId());
    }
}
