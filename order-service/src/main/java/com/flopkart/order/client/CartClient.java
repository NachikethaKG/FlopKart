package com.flopkart.order.client;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.cart.CartDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "CART-SERVICE", fallback = CartClientFallback.class)
public interface CartClient {

    @GetMapping("/api/v1/cart")
    ApiResponse<CartDto> getCart(@RequestHeader("X-User-Id") String userId);

    @DeleteMapping("/api/v1/cart")
    ApiResponse<Void> clearCart(@RequestHeader("X-User-Id") String userId);
}
