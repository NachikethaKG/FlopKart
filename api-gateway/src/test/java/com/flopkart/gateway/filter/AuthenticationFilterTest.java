package com.flopkart.gateway.filter;

import com.flopkart.gateway.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationFilterTest {

    private JwtUtil jwtUtil;
    private AuthenticationFilter authenticationFilter;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secret);
        authenticationFilter = new AuthenticationFilter(jwtUtil);
    }

    private String createValidToken(String userId, String email, String role) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claims(Map.of("userId", userId, "role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    @DisplayName("Public GET /api/v1/products passes through without authentication")
    void shouldAllowPublicProductGet() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/products").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        };

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted.get());
    }

    @Test
    @DisplayName("Public POST /api/v1/auth/login passes through without authentication")
    void shouldAllowPublicAuthEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainExecuted.set(true);
            return Mono.empty();
        };

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted.get());
    }

    @Test
    @DisplayName("Protected /api/v1/cart without Authorization header returns 401")
    void shouldRejectCartWithoutAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/cart").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected /api/v1/cart with invalid token returns 401")
    void shouldRejectCartWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("Protected /api/v1/cart with valid token propagates X-User-Id and X-User-Roles")
    void shouldPassValidTokenAndMutateHeaders() {
        String token = createValidToken("user-100", "test@flopkart.com", "ROLE_CUSTOMER");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/cart")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainExecuted.set(true);
            assertEquals("user-100", ex.getRequest().getHeaders().getFirst("X-User-Id"));
            assertEquals("ROLE_CUSTOMER", ex.getRequest().getHeaders().getFirst("X-User-Roles"));
            return Mono.empty();
        };

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted.get());
    }

    @Test
    @DisplayName("POST /api/v1/products requires authentication")
    void shouldProtectNonGetProductEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/products").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("POST /api/v1/products with CUSTOMER token returns 403 Forbidden")
    void shouldRejectCustomerProductWriteWithForbidden() {
        String token = createValidToken("user-100", "customer@flopkart.com", "ROLE_USER");

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    @DisplayName("POST /api/v1/products with MANAGER token passes through")
    void shouldAllowManagerProductWrite() {
        String token = createValidToken("mgr-1", "manager@flopkart.com", "MANAGER");

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        AtomicBoolean chainExecuted = new AtomicBoolean(false);
        GatewayFilterChain chain = ex -> {
            chainExecuted.set(true);
            assertEquals("mgr-1", ex.getRequest().getHeaders().getFirst("X-User-Id"));
            assertEquals("MANAGER", ex.getRequest().getHeaders().getFirst("X-User-Roles"));
            return Mono.empty();
        };

        StepVerifier.create(authenticationFilter.filter(exchange, chain))
                .verifyComplete();

        assertTrue(chainExecuted.get());
    }
}
