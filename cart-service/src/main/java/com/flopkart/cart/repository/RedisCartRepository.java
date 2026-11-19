package com.flopkart.cart.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flopkart.cart.model.Cart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RedisCartRepository implements CartRepository {

    private static final String CART_KEY_PREFIX = "cart:";

    private final RedisOperations<String, Object> redisOperations;
    private final ObjectMapper objectMapper;
    private final long ttlDays;

    public RedisCartRepository(RedisOperations<String, Object> redisOperations,
                               ObjectMapper objectMapper,
                               @Value("${cart.ttl-days:7}") long ttlDays) {
        this.redisOperations = redisOperations;
        this.objectMapper = objectMapper;
        this.ttlDays = ttlDays;
    }

    @Override
    public Optional<Cart> findByUserId(String userId) {
        String key = buildKey(userId);
        Object value = redisOperations.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (value instanceof Cart cart) {
            return Optional.of(cart);
        }
        Cart cart = objectMapper.convertValue(value, Cart.class);
        return Optional.ofNullable(cart);
    }

    @Override
    public Cart save(Cart cart) {
        cart.calculateTotals();
        String key = buildKey(cart.getUserId());
        redisOperations.opsForValue().set(key, cart, ttlDays, TimeUnit.DAYS);
        return cart;
    }

    @Override
    public void deleteByUserId(String userId) {
        String key = buildKey(userId);
        redisOperations.delete(key);
    }

    @Override
    public boolean existsByUserId(String userId) {
        String key = buildKey(userId);
        return Boolean.TRUE.equals(redisOperations.hasKey(key));
    }

    private String buildKey(String userId) {
        return CART_KEY_PREFIX + userId;
    }
}
