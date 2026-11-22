import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import axiosClient from '../api/axiosClient';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  
  const [cart, setCart] = useState({
    items: [],
    totalItems: 0,
    totalPrice: 0,
  });
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState({ visible: false, message: '', type: 'success' });

  const showToast = (message, type = 'success') => {
    setToast({ visible: true, message, type });
    setTimeout(() => {
      setToast((prev) => ({ ...prev, visible: false }));
    }, 3000);
  };

  const dismissToast = () => {
    setToast((prev) => ({ ...prev, visible: false }));
  };

  // Fetch cart from Redis via API Gateway
  const fetchCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart({ items: [], totalItems: 0, totalPrice: 0 });
      return;
    }

    setLoading(true);
    try {
      const res = await axiosClient.get('/cart');
      const cartData = res.data || res;
      setCart({
        items: cartData.items || [],
        totalItems: cartData.totalItems || 0,
        totalPrice: cartData.totalPrice || 0,
        updatedAt: cartData.updatedAt,
      });
    } catch (err) {
      console.error('Failed to fetch user cart', err);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  // Add Item
  const addItem = async (product, quantity = 1) => {
    if (!isAuthenticated) {
      showToast('Please sign in to add items to your cart.', 'info');
      return { success: false, requireAuth: true };
    }

    setLoading(true);
    try {
      const payload = {
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity: quantity,
        imageUrl: product.imageUrl || '',
      };

      const res = await axiosClient.post('/cart/items', payload);
      const cartData = res.data || res;
      setCart({
        items: cartData.items || [],
        totalItems: cartData.totalItems || 0,
        totalPrice: cartData.totalPrice || 0,
        updatedAt: cartData.updatedAt,
      });
      showToast(`Added ${quantity} unit(s) of "${product.name}" to cart.`);
      return { success: true };
    } catch (err) {
      console.error('Failed to add item to cart', err);
      showToast('Failed to add item to cart. Please try again.', 'error');
      return { success: false, error: err.message };
    } finally {
      setLoading(false);
    }
  };

  // Update Item Quantity
  const updateQuantity = async (productId, quantity) => {
    if (!isAuthenticated) return;
    
    if (quantity <= 0) {
      return removeItem(productId);
    }

    setLoading(true);
    try {
      const res = await axiosClient.put(`/cart/items/${productId}`, { quantity });
      const cartData = res.data || res;
      setCart({
        items: cartData.items || [],
        totalItems: cartData.totalItems || 0,
        totalPrice: cartData.totalPrice || 0,
        updatedAt: cartData.updatedAt,
      });
      showToast('Cart quantity updated.');
    } catch (err) {
      console.error('Failed to update cart quantity', err);
      showToast('Failed to update item quantity.', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Remove Item
  const removeItem = async (productId) => {
    if (!isAuthenticated) return;

    setLoading(true);
    try {
      const res = await axiosClient.delete(`/cart/items/${productId}`);
      const cartData = res.data || res;
      setCart({
        items: cartData.items || [],
        totalItems: cartData.totalItems || 0,
        totalPrice: cartData.totalPrice || 0,
        updatedAt: cartData.updatedAt,
      });
      showToast('Item removed from your cart.');
    } catch (err) {
      console.error('Failed to remove cart item', err);
      showToast('Failed to remove item.', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Clear Cart
  const clearCart = async () => {
    if (!isAuthenticated) return;

    setLoading(true);
    try {
      await axiosClient.delete('/cart');
      setCart({ items: [], totalItems: 0, totalPrice: 0 });
      showToast('Your shopping cart has been cleared.');
    } catch (err) {
      console.error('Failed to clear cart', err);
    } finally {
      setLoading(false);
    }
  };

  const value = {
    cart,
    items: cart.items || [],
    totalItemCount: cart.totalItems || 0,
    totalPrice: cart.totalPrice || 0,
    loading,
    toast,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    refreshCart: fetchCart,
    dismissToast,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}
