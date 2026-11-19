package com.flopkart.user.service;

import com.flopkart.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Generate token and extract claims successfully")
    void shouldGenerateAndExtractClaims() {
        User user = User.builder()
                .id(42L)
                .name("Alice Smith")
                .email("alice@example.com")
                .password("hashed_password")
                .role("ROLE_USER")
                .createdAt(LocalDateTime.now())
                .build();

        String token = jwtService.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        assertEquals("alice@example.com", jwtService.extractEmail(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("ROLE_USER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, "alice@example.com"));
        assertFalse(jwtService.isTokenValid(token, "other@example.com"));
        assertFalse(jwtService.isTokenExpired(token));
    }
}
