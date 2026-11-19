package com.flopkart.product.config;

import com.flopkart.product.entity.Category;
import com.flopkart.product.entity.Product;
import com.flopkart.product.repository.CategoryRepository;
import com.flopkart.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0 || productRepository.count() > 0) {
            return;
        }

        Category electronics = categoryRepository.save(new Category(null, "Electronics", "electronics"));
        Category fashion = categoryRepository.save(new Category(null, "Fashion", "fashion"));
        Category books = categoryRepository.save(new Category(null, "Books", "books"));
        Category homeKitchen = categoryRepository.save(new Category(null, "Home & Kitchen", "home-kitchen"));

        List<Product> products = List.of(
                Product.builder()
                        .name("Smartphone Ultra 5G")
                        .description("Flagship smartphone with 6.7 inch AMOLED display, 120Hz refresh rate, and 50MP camera.")
                        .price(new BigDecimal("799.99"))
                        .stockQuantity(50)
                        .category(electronics)
                        .imageUrl("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Noise-Cancelling Wireless Headphones")
                        .description("Over-ear wireless headphones with active noise cancellation and 40-hour battery life.")
                        .price(new BigDecimal("199.99"))
                        .stockQuantity(120)
                        .category(electronics)
                        .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Classic Cotton T-Shirt")
                        .description("100% premium combed organic cotton crewneck t-shirt.")
                        .price(new BigDecimal("24.99"))
                        .stockQuantity(200)
                        .category(fashion)
                        .imageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Performance Running Sneakers")
                        .description("Lightweight responsive foam running shoes designed for marathon endurance.")
                        .price(new BigDecimal("89.99"))
                        .stockQuantity(75)
                        .category(fashion)
                        .imageUrl("https://images.unsplash.com/photo-1542291026-7eec264c27ff")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Clean Architecture: Software Craftsmanship")
                        .description("A Craftsman's Guide to Software Structure and Design by Robert C. Martin.")
                        .price(new BigDecimal("34.99"))
                        .stockQuantity(40)
                        .category(books)
                        .imageUrl("https://images.unsplash.com/photo-1532012164546-f432f2e37b29")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Italian Espresso Coffee Machine")
                        .description("15-bar high pressure professional espresso coffee maker with milk frother.")
                        .price(new BigDecimal("149.99"))
                        .stockQuantity(30)
                        .category(homeKitchen)
                        .imageUrl("https://images.unsplash.com/photo-1517668808822-9ebb02ae2a0e")
                        .active(true)
                        .build()
        );

        productRepository.saveAll(products);
    }
}
