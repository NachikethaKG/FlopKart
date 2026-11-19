package com.flopkart.product.controller;

import com.flopkart.common.dto.ApiResponse;
import com.flopkart.common.dto.PageResponse;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.product.dto.CreateProductRequest;
import com.flopkart.product.dto.ProductStockResponse;
import com.flopkart.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "category", required = false) String category) {

        PageResponse<ProductDto> products = productService.getProducts(page, size, category);
        return ResponseEntity.ok(ApiResponse.success("Products retrieved successfully", products));
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable(name = "id") Long id) {
        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product retrieved successfully", product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDto created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Product created successfully", created));
    }

    @GetMapping("/{id:[0-9]+}/stock")
    public ResponseEntity<ApiResponse<ProductStockResponse>> getProductStock(@PathVariable(name = "id") Long id) {
        ProductStockResponse stock = productService.getProductStock(id);
        return ResponseEntity.ok(ApiResponse.success("Product stock retrieved successfully", stock));
    }
}
