package com.flopkart.cart.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CartModelTests {

    @Test
    @DisplayName("Adding items calculates correct total price and item counts")
    void shouldCalculateTotalsCorrectly() {
        Cart cart = new Cart("user-123");

        CartItem item1 = new CartItem(101L, "Laptop", new BigDecimal("1000.00"), 1, null);
        CartItem item2 = new CartItem(102L, "Mouse", new BigDecimal("50.00"), 2, null);

        cart.addItem(item1);
        cart.addItem(item2);

        assertEquals(2, cart.getItems().size());
        assertEquals(3, cart.getTotalItems());
        assertEquals(new BigDecimal("1100.00"), cart.getTotalPrice());

        // Increment quantity of existing item
        cart.addItem(new CartItem(102L, "Mouse", new BigDecimal("50.00"), 1, null));
        assertEquals(2, cart.getItems().size());
        assertEquals(4, cart.getTotalItems());
        assertEquals(new BigDecimal("1150.00"), cart.getTotalPrice());

        // Update quantity
        cart.updateItemQuantity(102L, 1);
        assertEquals(2, cart.getTotalItems());
        assertEquals(new BigDecimal("1050.00"), cart.getTotalPrice());

        // Remove item
        cart.removeItem(101L);
        assertEquals(1, cart.getItems().size());
        assertEquals(1, cart.getTotalItems());
        assertEquals(new BigDecimal("50.00"), cart.getTotalPrice());

        // Clear
        cart.clear();
        assertEquals(0, cart.getItems().size());
        assertEquals(0, cart.getTotalItems());
        assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
    }
}
