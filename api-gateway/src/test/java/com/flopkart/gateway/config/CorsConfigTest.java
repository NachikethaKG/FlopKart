package com.flopkart.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CorsConfigTest {

    @Test
    @DisplayName("CORS allows origin specified in configuration")
    void testAllowedOriginPreflight() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "https://flopkart.vercel.app,http://localhost:5173");
        WebFilter filter = corsConfig.corsFilter();

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8080/api/v1/products")
                .header(HttpHeaders.ORIGIN, "https://flopkart.vercel.app")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        assertEquals("https://flopkart.vercel.app", exchange.getResponse().getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    @DisplayName("CORS rejects origin not in allowed list")
    void testDisallowedOrigin() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:5173");
        WebFilter filter = corsConfig.corsFilter();

        MockServerHttpRequest request = MockServerHttpRequest
                .options("http://localhost:8080/api/v1/products")
                .header(HttpHeaders.ORIGIN, "https://unauthorized-domain.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = ex -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }
}
