package com.flopkart.cart.repository;

import com.flopkart.cart.model.Cart;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByUserId(String userId);

    Cart save(Cart cart);

    void deleteByUserId(String userId);

    boolean existsByUserId(String userId);
}
