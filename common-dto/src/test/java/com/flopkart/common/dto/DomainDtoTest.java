package com.flopkart.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flopkart.common.dto.order.OrderDto;
import com.flopkart.common.dto.order.OrderItemDto;
import com.flopkart.common.dto.product.ProductDto;
import com.flopkart.common.dto.user.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainDtoTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should build ProductDto and serialize to JSON")
    void shouldBuildProductDto() throws Exception {
        ProductDto product = ProductDto.builder()
                .id(1L)
                .name("Wireless Noise Canceling Headphones")
                .description("Premium over-ear headphones")
                .price(new BigDecimal("199.99"))
                .stockQuantity(50)
                .category("Electronics")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Wireless Noise Canceling Headphones");
        assertThat(product.getPrice()).isEqualTo(new BigDecimal("199.99"));

        String json = objectMapper.writeValueAsString(product);
        assertThat(json).contains("\"name\":\"Wireless Noise Canceling Headphones\"");
        assertThat(json).contains("\"price\":199.99");
    }

    @Test
    @DisplayName("Should build UserDto and serialize to JSON")
    void shouldBuildUserDto() throws Exception {
        UserDto user = UserDto.builder()
                .id(101L)
                .username("john_doe")
                .email("john@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("ROLE_CUSTOMER")
                .active(true)
                .build();

        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");

        String json = objectMapper.writeValueAsString(user);
        assertThat(json).contains("\"username\":\"john_doe\"");
    }

    @Test
    @DisplayName("Should build OrderDto with OrderItemDto and auto-calculate subtotal")
    void shouldBuildOrderDtoWithItems() throws Exception {
        OrderItemDto item1 = OrderItemDto.builder()
                .productId(1L)
                .productName("Headphones")
                .quantity(2)
                .unitPrice(new BigDecimal("150.00"))
                .build();

        assertThat(item1.getSubtotal()).isEqualByComparingTo("300.00");

        OrderDto order = OrderDto.builder()
                .id(5001L)
                .orderNumber("ORD-2026-0001")
                .userId(101L)
                .totalAmount(new BigDecimal("300.00"))
                .status("PENDING")
                .items(List.of(item1))
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getProductName()).isEqualTo("Headphones");

        String json = objectMapper.writeValueAsString(order);
        assertThat(json).contains("\"orderNumber\":\"ORD-2026-0001\"");
        assertThat(json).contains("\"subtotal\":300.00");
    }
}
