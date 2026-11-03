package com.flopkart.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should create successful response with default message")
    void shouldCreateSuccessResponseWithData() {
        String testData = "test-payload";
        ApiResponse<String> response = ApiResponse.success(testData);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Operation completed successfully");
        assertThat(response.getData()).isEqualTo(testData);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create successful response with custom message and data")
    void shouldCreateSuccessResponseWithCustomMessage() {
        ApiResponse<Integer> response = ApiResponse.success("Custom success", 42);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Custom success");
        assertThat(response.getData()).isEqualTo(42);
    }

    @Test
    @DisplayName("Should create 201 Created response")
    void shouldCreateCreatedResponse() {
        ApiResponse<String> response = ApiResponse.created("Resource created", "id-123");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getMessage()).isEqualTo("Resource created");
        assertThat(response.getData()).isEqualTo("id-123");
    }

    @Test
    @DisplayName("Should create error response")
    void shouldCreateErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error("Something went wrong", 404);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("Something went wrong");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should build response with Builder pattern")
    void shouldBuildWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .status(200)
                .message("Built successfully")
                .data("Data")
                .timestamp(now)
                .build();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("Built successfully");
        assertThat(response.getData()).isEqualTo("Data");
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should serialize and deserialize to JSON properly")
    void shouldSerializeAndDeserializeJson() throws Exception {
        ApiResponse<String> original = ApiResponse.success("Fetched", "Sample Data");

        String json = objectMapper.writeValueAsString(original);
        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"message\":\"Fetched\"");
        assertThat(json).contains("\"data\":\"Sample Data\"");

        ApiResponse<?> deserialized = objectMapper.readValue(json, ApiResponse.class);
        assertThat(deserialized.isSuccess()).isTrue();
        assertThat(deserialized.getMessage()).isEqualTo("Fetched");
        assertThat(deserialized.getData()).isEqualTo("Sample Data");
    }
}
