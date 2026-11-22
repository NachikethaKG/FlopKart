package com.flopkart.order.client;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.cart.CartDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CartClientFallback implements CartClient {

    private static final Logger log = LoggerFactory.getLogger(CartClientFallback.class);

    @Override
    public ApiResponse<CartDto> getCart(String userId) {
        log.warn("Circuit breaker triggered fallback for getCart: {}", userId);
        return ApiResponse.error("Cart service is currently unavailable. Circuit breaker fallback activated.", 503);
    }

    @Override
    public ApiResponse<Void> clearCart(String userId) {
        log.warn("Circuit breaker triggered fallback for clearCart: {}", userId);
        return ApiResponse.error("Cart service is currently unavailable for clearCart. Circuit breaker fallback activated.", 503);
    }
}
