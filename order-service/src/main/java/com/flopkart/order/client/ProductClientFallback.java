package com.flopkart.order.client;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.common.dto.product.StockReleaseRequest;
import com.flopkart.common.dto.product.StockReservationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductClientFallback implements ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClientFallback.class);

    @Override
    public ApiResponse<ProductDto> getProductById(Long id) {
        log.warn("Circuit breaker triggered fallback for getProductById: {}", id);
        return ApiResponse.error("Product service is currently unavailable. Circuit breaker fallback activated.", 503);
    }

    @Override
    public ApiResponse<Void> reserveStock(StockReservationRequest request) {
        log.warn("Circuit breaker triggered fallback for reserveStock");
        return ApiResponse.error("Product service is currently unavailable for stock reservation. Circuit breaker fallback activated.", 503);
    }

    @Override
    public ApiResponse<Void> releaseStock(StockReleaseRequest request) {
        log.warn("Circuit breaker triggered fallback for releaseStock");
        return ApiResponse.error("Product service is currently unavailable for stock release. Circuit breaker fallback activated.", 503);
    }
}
