package com.flopkart.order.client;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.common.dto.product.StockItemDto;
import com.flopkart.common.dto.product.StockReleaseRequest;
import com.flopkart.common.dto.product.StockReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductClientFallbackTest {

    private ProductClientFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new ProductClientFallback();
    }

    @Test
    @DisplayName("ProductClientFallback returns 503 error for getProductById")
    void shouldReturnFallbackForGetProductById() {
        ApiResponse<ProductDto> response = fallback.getProductById(1L);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatus());
        assertTrue(response.getMessage().contains("Circuit breaker fallback"));
    }

    @Test
    @DisplayName("ProductClientFallback returns 503 error for reserveStock")
    void shouldReturnFallbackForReserveStock() {
        StockReservationRequest request = new StockReservationRequest(List.of(new StockItemDto(1L, 2)));
        ApiResponse<Void> response = fallback.reserveStock(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatus());
        assertTrue(response.getMessage().contains("Circuit breaker fallback"));
    }

    @Test
    @DisplayName("ProductClientFallback returns 503 error for releaseStock")
    void shouldReturnFallbackForReleaseStock() {
        StockReleaseRequest request = new StockReleaseRequest("ORD-1", List.of(new StockItemDto(1L, 2)));
        ApiResponse<Void> response = fallback.releaseStock(request);

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(503, response.getStatus());
        assertTrue(response.getMessage().contains("Circuit breaker fallback"));
    }
}
