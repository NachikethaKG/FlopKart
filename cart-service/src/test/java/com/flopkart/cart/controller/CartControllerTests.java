package com.flopkart.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flopkart.cart.dto.AddItemRequest;
import com.flopkart.cart.dto.UpdateQuantityRequest;
import com.flopkart.cart.model.Cart;
import com.flopkart.cart.model.CartItem;
import com.flopkart.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("GET /api/v1/cart with X-User-Id header returns 200 OK and cart")
    void shouldReturnCartWhenHeaderPresent() throws Exception {
        Cart cart = new Cart("user-1");
        cart.addItem(new CartItem(10L, "Headphones", new BigDecimal("99.99"), 1, null));
        when(cartService.getCart("user-1")).thenReturn(cart);

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user-1"))
                .andExpect(jsonPath("$.data.items[0].productName").value("Headphones"))
                .andExpect(jsonPath("$.data.totalPrice").value(99.99));
    }

    @Test
    @DisplayName("GET /api/v1/cart without X-User-Id header returns 400 Bad Request")
    void shouldFailWhenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items adds item and returns updated cart")
    void shouldAddItemToCart() throws Exception {
        AddItemRequest request = new AddItemRequest(10L, "Headphones", new BigDecimal("99.99"), 2, "http://image.jpg");
        Cart cart = new Cart("user-1");
        cart.addItem(new CartItem(10L, "Headphones", new BigDecimal("99.99"), 2, "http://image.jpg"));

        when(cartService.addItem(eq("user-1"), any(AddItemRequest.class))).thenReturn(cart);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(2))
                .andExpect(jsonPath("$.data.totalPrice").value(199.98));
    }

    @Test
    @DisplayName("PUT /api/v1/cart/items/{productId} updates item quantity")
    void shouldUpdateItemQuantity() throws Exception {
        UpdateQuantityRequest request = new UpdateQuantityRequest(5);
        Cart cart = new Cart("user-1");
        cart.addItem(new CartItem(10L, "Headphones", new BigDecimal("99.99"), 5, null));

        when(cartService.updateItemQuantity("user-1", 10L, 5)).thenReturn(cart);

        mockMvc.perform(put("/api/v1/cart/items/10")
                        .header("X-User-Id", "user-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(5));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{productId} removes item from cart")
    void shouldRemoveItemFromCart() throws Exception {
        Cart cart = new Cart("user-1");
        when(cartService.removeItem("user-1", 10L)).thenReturn(cart);

        mockMvc.perform(delete("/api/v1/cart/items/10")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart clears cart upon checkout")
    void shouldClearCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                        .header("X-User-Id", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart cleared successfully"));

        verify(cartService).clearCart("user-1");
    }
}
