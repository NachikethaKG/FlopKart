package com.flopkart.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Immutable compensating event emitted to release previously reserved inventory upon order cancellation/payment failure.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record StockReleaseEvent(
        String orderNumber,
        List<OrderItemDto> items,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public StockReleaseEvent {
        items = (items != null) ? List.copyOf(items) : List.of();
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
