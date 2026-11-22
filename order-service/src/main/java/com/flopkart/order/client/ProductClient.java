package com.flopkart.order.client;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.common.dto.product.StockReleaseRequest;
import com.flopkart.common.dto.product.StockReservationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PRODUCT-SERVICE", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable("id") Long id);

    @PostMapping("/api/v1/products/reserve-stock")
    ApiResponse<Void> reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/api/v1/products/release-stock")
    ApiResponse<Void> releaseStock(@RequestBody StockReleaseRequest request);
}
