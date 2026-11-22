package com.flopkart.common.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Immutable event record representing an item in an order event payload.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItemDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal price
) implements Serializable {

    private static final long serialVersionUID = 1L;

    public OrderItemDto {
        if (quantity != null && quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    @JsonIgnore
    public BigDecimal subtotal() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
