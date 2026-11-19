package com.flopkart.cart.service;

import com.flopkart.cart.dto.AddItemRequest;
import com.flopkart.cart.model.Cart;
import com.flopkart.cart.model.CartItem;
import com.flopkart.cart.repository.CartRepository;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    public CartServiceImpl(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public Cart getCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> new Cart(userId));
    }

    @Override
    public Cart addItem(String userId, AddItemRequest request) {
        Cart cart = getCart(userId);
        CartItem item = new CartItem(
                request.getProductId(),
                request.getProductName(),
                request.getPrice(),
                request.getQuantity(),
                request.getImageUrl()
        );
        cart.addItem(item);
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(String userId, Long productId, int quantity) {
        Cart cart = getCart(userId);
        cart.updateItemQuantity(productId, quantity);
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItem(String userId, Long productId) {
        Cart cart = getCart(userId);
        cart.removeItem(productId);
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.deleteByUserId(userId);
    }
}
