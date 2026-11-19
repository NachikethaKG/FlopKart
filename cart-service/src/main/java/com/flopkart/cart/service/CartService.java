package com.flopkart.cart.service;

import com.flopkart.cart.dto.AddItemRequest;
import com.flopkart.cart.model.Cart;

public interface CartService {

    Cart getCart(String userId);

    Cart addItem(String userId, AddItemRequest request);

    Cart updateItemQuantity(String userId, Long productId, int quantity);

    Cart removeItem(String userId, Long productId);

    void clearCart(String userId);
}
