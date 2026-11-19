package com.flopkart.cart.service;

import com.flopkart.cart.dto.AddItemRequest;
import com.flopkart.cart.model.Cart;
import com.flopkart.cart.repository.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTests {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart testCart;

    @BeforeEach
    void setUp() {
        testCart = new Cart("user-1");
    }

    @Test
    @DisplayName("Get cart returns existing cart or creates empty cart if none")
    void shouldGetCart() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(testCart));

        Cart cart = cartService.getCart("user-1");
        assertNotNull(cart);
        assertEquals("user-1", cart.getUserId());

        when(cartRepository.findByUserId("user-2")).thenReturn(Optional.empty());
        Cart newCart = cartService.getCart("user-2");
        assertNotNull(newCart);
        assertEquals("user-2", newCart.getUserId());
    }

    @Test
    @DisplayName("Add item delegates to cart repository save")
    void shouldAddItem() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddItemRequest request = new AddItemRequest(10L, "Shoes", new BigDecimal("75.00"), 2, "http://shoes.jpg");
        Cart updated = cartService.addItem("user-1", request);

        assertEquals(1, updated.getItems().size());
        assertEquals(2, updated.getTotalItems());
        assertEquals(new BigDecimal("150.00"), updated.getTotalPrice());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Update item quantity adjusts cart totals")
    void shouldUpdateQuantity() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddItemRequest request = new AddItemRequest(10L, "Shoes", new BigDecimal("75.00"), 2, "http://shoes.jpg");
        cartService.addItem("user-1", request);

        Cart updated = cartService.updateItemQuantity("user-1", 10L, 4);
        assertEquals(4, updated.getTotalItems());
        assertEquals(new BigDecimal("300.00"), updated.getTotalPrice());
    }

    @Test
    @DisplayName("Remove item removes from cart")
    void shouldRemoveItem() {
        when(cartRepository.findByUserId("user-1")).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AddItemRequest request = new AddItemRequest(10L, "Shoes", new BigDecimal("75.00"), 2, "http://shoes.jpg");
        cartService.addItem("user-1", request);

        Cart updated = cartService.removeItem("user-1", 10L);
        assertEquals(0, updated.getItems().size());
        assertEquals(0, updated.getTotalItems());
    }

    @Test
    @DisplayName("Clear cart calls delete on repository")
    void shouldClearCart() {
        cartService.clearCart("user-1");
        verify(cartRepository).deleteByUserId("user-1");
    }
}
