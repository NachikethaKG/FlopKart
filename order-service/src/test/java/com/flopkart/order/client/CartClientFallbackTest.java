package com.flopkart.order.client;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.cart.CartDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartClientFallbackTest {

    private CartClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new CartClientFallback();
    }

    @Test
    @DisplayName("CartClientFallback returns 503 error for getCart")
    void shouldReturnFallbackForGetCart() {
        ApiResponse<CartDto> response = fallback.getCart("user-1");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatus());
        assertTrue(response.getMessage().contains("Circuit breaker fallback"));
    }

    @Test
    @DisplayName("CartClientFallback returns 503 error for clearCart")
    void shouldReturnFallbackForClearCart() {
        ApiResponse<Void> response = fallback.clearCart("user-1");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatus());
        assertTrue(response.getMessage().contains("Circuit breaker fallback"));
    }
}
