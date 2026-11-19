package com.flopkart.product.service;

import com.flopkart.common.dto.PageResponse;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.product.dto.CreateProductRequest;
import com.flopkart.product.dto.ProductStockResponse;
import com.flopkart.product.entity.Category;
import com.flopkart.product.entity.Product;
import com.flopkart.product.exception.ResourceNotFoundException;
import com.flopkart.product.repository.CategoryRepository;
import com.flopkart.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

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
