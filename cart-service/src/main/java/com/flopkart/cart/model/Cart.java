package com.flopkart.cart.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Cart implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private List<CartItem> items = new ArrayList<>();
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private Integer totalItems = 0;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;

    public Cart() {
        this.items = new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
    }

    public Cart(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    public void addItem(CartItem newItem) {
        if (newItem == null) return;
        if (items == null) items = new ArrayList<>();

        boolean found = false;
        for (CartItem existing : items) {
            if (existing.getProductId().equals(newItem.getProductId())) {
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(newItem);
        }
        calculateTotals();
    }

    public void updateItemQuantity(Long productId, int quantity) {
        if (items == null) return;
        if (quantity <= 0) {
            removeItem(productId);
            return;
        }

        for (CartItem existing : items) {
            if (existing.getProductId().equals(productId)) {
                existing.setQuantity(quantity);
                break;
            }
        }
        calculateTotals();
    }

    public void removeItem(Long productId) {
        if (items == null) return;
        items.removeIf(item -> item.getProductId().equals(productId));
        calculateTotals();
    }

    public void clear() {
        if (items != null) {
            items.clear();
        }
        calculateTotals();
    }

    public void calculateTotals() {
        if (items == null) {
            items = new ArrayList<>();
        }
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (CartItem item : items) {
            item.calculateItemTotal();
            sum = sum.add(item.getItemTotal());
            count += item.getQuantity();
        }
        this.totalPrice = sum;
        this.totalItems = count;
        this.updatedAt = LocalDateTime.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        calculateTotals();
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
