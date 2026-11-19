package com.flopkart.cart.controller;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.cart.dto.AddItemRequest;
import com.flopkart.cart.dto.UpdateQuantityRequest;
import com.flopkart.cart.model.Cart;
import com.flopkart.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(
            @RequestHeader(name = "X-User-Id") String userId) {
        Cart cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<Cart>> addItem(
            @RequestHeader(name = "X-User-Id") String userId,
            @Valid @RequestBody AddItemRequest request) {
        Cart cart = cartService.addItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart successfully", cart));
    }

    @PutMapping("/items/{productId:[0-9]+}")
    public ResponseEntity<ApiResponse<Cart>> updateItemQuantity(
            @RequestHeader(name = "X-User-Id") String userId,
            @PathVariable(name = "productId") Long productId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        Cart cart = cartService.updateItemQuantity(userId, productId, request.getQuantity());
        return ResponseEntity.ok(ApiResponse.success("Cart item quantity updated", cart));
    }

    @DeleteMapping("/items/{productId:[0-9]+}")
    public ResponseEntity<ApiResponse<Cart>> removeItem(
            @RequestHeader(name = "X-User-Id") String userId,
            @PathVariable(name = "productId") Long productId) {
        Cart cart = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Cart item removed successfully", cart));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(name = "X-User-Id") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully", null));
    }
}
