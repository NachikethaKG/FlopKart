package com.flopkart.product.dto;

public class UpdateStockRequest {

    private Integer delta;
    private Integer quantity;

    public UpdateStockRequest() {
    }

    public UpdateStockRequest(Integer delta, Integer quantity) {
        this.delta = delta;
        this.quantity = quantity;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
