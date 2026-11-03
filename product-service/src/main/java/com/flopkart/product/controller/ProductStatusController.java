package com.flopkart.product.controller;

import com.flopkart.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controller exposing test and status endpoints for Product Service.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductStatusController {

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> statusInfo = new LinkedHashMap<>();
        statusInfo.put("service", "product-service");
        statusInfo.put("status", "UP");
        statusInfo.put("port", 8082);
        statusInfo.put("message", "Product Service is running and accessible through API Gateway");

        return ResponseEntity.ok(ApiResponse.success("Product Service is operational", statusInfo));
    }
}
