package com.flopkart.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false"
})
class ApiGatewayApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Application context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Health endpoint returns UP")
    void healthEndpointReturnsUp() {
        webTestClient.get()
                .uri("http://localhost:" + port + "/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    @Test
    @DisplayName("CORS preflight request for localhost:5173 is allowed")
    void corsPreflightForViteAllowed() {
        webTestClient.options()
                .uri("http://localhost:" + port + "/api/test")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:5173")
                .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true")
                .expectHeader().valueEquals("Access-Control-Max-Age", "3600");
    }

    @Test
    @DisplayName("CORS preflight request for localhost:3000 is allowed")
    void corsPreflightForReactAllowed() {
        webTestClient.options()
                .uri("http://localhost:" + port + "/api/test")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000")
                .expectHeader().valueEquals("Access-Control-Allow-Credentials", "true");
    }

    @Test
    @DisplayName("CORS preflight request for unauthorized origin is rejected")
    void corsPreflightForUnauthorizedOriginRejected() {
        webTestClient.options()
                .uri("http://localhost:" + port + "/api/test")
                .header("Origin", "http://malicious-site.com")
                .header("Access-Control-Request-Method", "GET")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("Accessing protected /api/v1/cart without token returns 401 Unauthorized")
    void cartWithoutTokenReturns401() {
        webTestClient.get()
                .uri("http://localhost:" + port + "/api/v1/cart")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header");
    }

    @Test
    @DisplayName("Accessing protected /api/v1/cart with invalid token returns 401 Unauthorized")
    void cartWithInvalidTokenReturns401() {
        webTestClient.get()
                .uri("http://localhost:" + port + "/api/v1/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer bogus.token.xyz")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.message").isEqualTo("Invalid or expired JWT token");
    }

    @Test
    @DisplayName("Accessing POST /api/v1/products without token returns 401 Unauthorized")
    void postProductsWithoutTokenReturns401() {
        webTestClient.post()
                .uri("http://localhost:" + port + "/api/v1/products")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(401);
    }
}
