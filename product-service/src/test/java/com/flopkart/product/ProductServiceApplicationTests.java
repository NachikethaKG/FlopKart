package com.flopkart.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flopkart.product.dto.CreateProductRequest;
import com.flopkart.product.entity.Category;
import com.flopkart.product.entity.Product;
import com.flopkart.product.repository.CategoryRepository;
import com.flopkart.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class ProductServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        testCategory = categoryRepository.save(new Category(null, "Electronics", "electronics"));
        testProduct = productRepository.save(Product.builder()
                .name("Test Phone")
                .description("Smart phone for testing")
                .price(new BigDecimal("499.99"))
                .stockQuantity(25)
                .imageUrl("http://example.com/phone.jpg")
                .category(testCategory)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("GET /api/v1/products/status returns 200 OK and expected JSON message")
    void shouldReturnProductServiceStatus() throws Exception {
        mockMvc.perform(get("/api/v1/products/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product Service is operational"))
                .andExpect(jsonPath("$.data.service").value("product-service"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.port").value(8082));
    }

    @Test
    @DisplayName("GET /actuator/health returns 200 OK with UP status")
    void shouldReturnHealthUp() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("GET /api/v1/products returns paginated product data from database")
    void shouldReturnPaginatedProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Phone"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.pageNumber").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/products?category=electronics filters correctly")
    void shouldFilterProductsByCategory() throws Exception {
        Category clothing = categoryRepository.save(new Category(null, "Clothing", "clothing"));
        productRepository.save(Product.builder()
                .name("T-Shirt")
                .description("Cotton shirt")
                .price(new BigDecimal("19.99"))
                .stockQuantity(50)
                .category(clothing)
                .active(true)
                .build());

        mockMvc.perform(get("/api/v1/products?category=electronics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Phone"));

        mockMvc.perform(get("/api/v1/products?category=clothing")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].name").value("T-Shirt"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} returns single product details")
    void shouldReturnSingleProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + testProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.name").value("Test Phone"))
                .andExpect(jsonPath("$.data.stockQuantity").value(25))
                .andExpect(jsonPath("$.data.category").value("Electronics"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} returns 404 for nonexistent product")
    void shouldReturn404ForNonexistentProduct() throws Exception {
        mockMvc.perform(get("/api/v1/products/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/products creates product successfully")
    void shouldCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Gaming Laptop",
                "High performance laptop",
                new BigDecimal("1299.99"),
                15,
                "Computers",
                "http://example.com/laptop.jpg",
                true
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("Gaming Laptop"))
                .andExpect(jsonPath("$.data.category").value("Computers"));
    }

    @Test
    @DisplayName("GET /api/v1/products/{id}/stock returns accurate stock quantity")
    void shouldReturnProductStock() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + testProduct.getId() + "/stock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.stockQuantity").value(25))
                .andExpect(jsonPath("$.data.inStock").value(true));
    }
}
