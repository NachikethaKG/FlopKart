package com.flopkart.product.service;

import com.flopkart.common.dto.PageResponse;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.common.dto.product.StockItemDto;
import com.flopkart.common.dto.product.StockReleaseRequest;
import com.flopkart.common.dto.product.StockReservationRequest;
import com.flopkart.product.dto.CreateProductRequest;
import com.flopkart.product.dto.ProductStockResponse;
import com.flopkart.product.dto.UpdatePriceRequest;
import com.flopkart.product.dto.UpdateStockRequest;
import com.flopkart.product.entity.Category;
import com.flopkart.product.entity.Product;
import com.flopkart.product.exception.InsufficientStockException;
import com.flopkart.product.exception.ResourceNotFoundException;
import com.flopkart.product.repository.CategoryRepository;
import com.flopkart.product.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductDto> getProducts(int page, int size, String category) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> productPage;

        if (category != null && !category.trim().isEmpty()) {
            productPage = productRepository.findActiveWithCategoryFilter(category.trim(), pageable);
        } else {
            productPage = productRepository.findByActiveTrue(pageable);
        }

        List<ProductDto> content = productPage.getContent().stream()
                .map(this::mapToDto)
                .toList();

        return PageResponse.of(content, productPage.getNumber(), productPage.getSize(), productPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDto(product);
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        Category category = null;
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            String catIdentifier = request.getCategory().trim();
            category = categoryRepository.findBySlug(catIdentifier.toLowerCase())
                    .or(() -> categoryRepository.findByNameIgnoreCase(catIdentifier))
                    .orElseGet(() -> {
                        String slug = catIdentifier.toLowerCase().replaceAll("[^a-z0-9]+", "-");
                        return categoryRepository.save(new Category(null, catIdentifier, slug));
                    });
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ProductStockResponse getProductStock(Long id) {
        Product product = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return new ProductStockResponse(
                product.getId(),
                product.getStockQuantity(),
                product.getStockQuantity() > 0
        );
    }

    @Transactional
    public void reserveStock(StockReservationRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Reservation request must contain at least one item");
        }

        log.info("Processing stock reservation for {} items", request.getItems().size());

        // Validate and deduct atomically
        for (StockItemDto item : request.getItems()) {
            Product product = productRepository.findByIdWithLock(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + item.getProductId()));

            if (product.getStockQuantity() < item.getQuantity()) {
                log.warn("Insufficient stock for product ID {}: requested {}, available {}",
                        item.getProductId(), item.getQuantity(), product.getStockQuantity());
                throw new InsufficientStockException(String.format(
                        "Insufficient stock for product '%s' (ID %d): requested %d, available %d",
                        product.getName(), item.getProductId(), item.getQuantity(), product.getStockQuantity()));
            }

            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
            log.info("Reserved {} units for product ID {}, remaining stock: {}",
                    item.getQuantity(), item.getProductId(), product.getStockQuantity());
        }
    }

    @Transactional
    public void releaseStock(StockReleaseRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("Empty stock release request received, skipping");
            return;
        }

        log.info("Releasing stock for order '{}', {} items", request.getOrderNumber(), request.getItems().size());
        for (StockItemDto item : request.getItems()) {
            productRepository.findByIdWithLock(item.getProductId())
                    .ifPresentOrElse(product -> {
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                        log.info("Restored {} units to product ID {}, new stock: {}",
                                item.getQuantity(), item.getProductId(), product.getStockQuantity());
                    }, () -> log.warn("Cannot release stock: product not found with id {}", item.getProductId()));
        }
    }

    @Transactional
    public ProductDto updateStock(Long id, UpdateStockRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getQuantity() != null) {
            if (request.getQuantity() < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative");
            }
            product.setStockQuantity(request.getQuantity());
        } else if (request.getDelta() != null) {
            int newQuantity = product.getStockQuantity() + request.getDelta();
            if (newQuantity < 0) {
                throw new IllegalArgumentException("Stock quantity cannot drop below zero");
            }
            product.setStockQuantity(newQuantity);
        } else {
            throw new IllegalArgumentException("Either delta or quantity must be provided");
        }

        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional
    public ProductDto updatePrice(Long id, UpdatePriceRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        product.setPrice(request.getPrice());
        Product saved = productRepository.save(product);
        return mapToDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setActive(false);
        productRepository.save(product);
    }

    public ProductDto mapToDto(Product product) {
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .category(categoryName)
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
