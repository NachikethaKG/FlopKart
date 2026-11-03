package com.flopkart.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class ProductServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

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
}
