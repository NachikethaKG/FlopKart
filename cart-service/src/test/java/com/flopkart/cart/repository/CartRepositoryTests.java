package com.flopkart.cart.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flopkart.cart.model.Cart;
import com.flopkart.cart.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartRepositoryTests {

    @Mock
    private RedisOperations<String, Object> redisOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisCartRepository cartRepository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        cartRepository = new RedisCartRepository(redisOperations, objectMapper, 7);
    }

    @Test
    @DisplayName("Save cart stores under key cart:{userId} with 7-day TTL")
    void shouldSaveCartWithTtl() {
        when(redisOperations.opsForValue()).thenReturn(valueOperations);

        Cart cart = new Cart("user-999");
        cart.addItem(new CartItem(1L, "Book", new BigDecimal("15.00"), 2, null));

        Cart saved = cartRepository.save(cart);
        assertNotNull(saved);

        verify(valueOperations).set("cart:user-999", cart, 7L, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("Find cart by userId reads from cart:{userId}")
    void shouldFindByUserId() {
        when(redisOperations.opsForValue()).thenReturn(valueOperations);
        Cart expected = new Cart("user-999");
        when(valueOperations.get("cart:user-999")).thenReturn(expected);

        Optional<Cart> result = cartRepository.findByUserId("user-999");
        assertTrue(result.isPresent());
        assertEquals("user-999", result.get().getUserId());
    }

    @Test
    @DisplayName("Delete cart removes key cart:{userId}")
    void shouldDeleteCart() {
        cartRepository.deleteByUserId("user-999");
        verify(redisOperations).delete("cart:user-999");
    }
}
