package com.flopkart.product.dto;

public class ProductStockResponse {

    private Long productId;
    private Integer stockQuantity;
    private Boolean inStock;

    public ProductStockResponse() {
    }

    public ProductStockResponse(Long productId, Integer stockQuantity, Boolean inStock) {
        this.productId = productId;
        this.stockQuantity = stockQuantity;
        this.inStock = inStock;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
}
