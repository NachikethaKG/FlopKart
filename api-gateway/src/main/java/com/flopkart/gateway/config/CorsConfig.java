package com.flopkart.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Global CORS filter allowing React clients (http://localhost:5173 and http://localhost:3000).
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebFilter corsFilter() {
        return new OrderedCorsWebFilter();
    }

    private static class OrderedCorsWebFilter implements WebFilter, Ordered {

        private final UrlBasedCorsConfigurationSource source;
        private final CorsProcessor processor;

        public OrderedCorsWebFilter() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOriginPatterns(List.of("http://localhost:5173", "http://localhost:3000"));
            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);

            this.source = new UrlBasedCorsConfigurationSource();
            this.source.registerCorsConfiguration("/**", config);
            this.processor = new DefaultCorsProcessor();
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            ServerHttpRequest request = exchange.getRequest();
            if (CorsUtils.isCorsRequest(request)) {
                CorsConfiguration corsConfiguration = this.source.getCorsConfiguration(exchange);
                boolean isValid = this.processor.process(corsConfiguration, exchange);
                if (!isValid) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (CorsUtils.isPreFlightRequest(request)) {
                    exchange.getResponse().setStatusCode(HttpStatus.OK);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        }
    }
}
