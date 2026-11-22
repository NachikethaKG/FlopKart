package com.flopkart.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable event emitted when an order has been initiated and reserved.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderPlacedEvent(
        String orderNumber,
        String userId,
        String userEmail,
        BigDecimal totalAmount,
        List<OrderItemDto> items,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public OrderPlacedEvent {
        items = (items != null) ? List.copyOf(items) : List.of();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
