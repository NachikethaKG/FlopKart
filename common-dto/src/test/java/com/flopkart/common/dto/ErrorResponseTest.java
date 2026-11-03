package com.flopkart.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should create standard error response using of factory method")
    void shouldCreateStandardErrorResponse() {
        ErrorResponse error = ErrorResponse.of(404, "Not Found", "Product with ID 99 not found", "/api/v1/products/99");

        assertThat(error.isSuccess()).isFalse();
        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getError()).isEqualTo("Not Found");
        assertThat(error.getMessage()).isEqualTo("Product with ID 99 not found");
        assertThat(error.getPath()).isEqualTo("/api/v1/products/99");
        assertThat(error.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create validation error response")
    void shouldCreateValidationErrorResponse() {
        List<ValidationError> validationErrors = List.of(
                ValidationError.of("name", "Product name must not be blank"),
                ValidationError.of("price", -10, "Price must be greater than zero")
        );

        ErrorResponse response = ErrorResponse.validation(400, "Invalid input data", "/api/v1/products", validationErrors);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Validation Failed");
        assertThat(response.getValidationErrors()).hasSize(2);
        assertThat(response.getValidationErrors().get(0).getField()).isEqualTo("name");
        assertThat(response.getValidationErrors().get(1).getRejectedValue()).isEqualTo(-10);
    }

    @Test
    @DisplayName("Should serialize to JSON properly")
    void shouldSerializeJson() throws Exception {
        ErrorResponse error = ErrorResponse.of(500, "Internal Server Error", "Database connection failed", "/api/v1/orders");

        String json = objectMapper.writeValueAsString(error);
        assertThat(json).contains("\"status\":500");
        assertThat(json).contains("\"error\":\"Internal Server Error\"");
        assertThat(json).contains("\"message\":\"Database connection failed\"");
        assertThat(json).contains("\"path\":\"/api/v1/orders\"");
    }
}
