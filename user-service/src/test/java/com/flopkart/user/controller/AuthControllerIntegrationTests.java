package com.flopkart.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flopkart.user.dto.LoginRequest;
import com.flopkart.user.dto.RegisterRequest;
import com.flopkart.user.entity.User;
import com.flopkart.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register hashes password, saves user, and returns token")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = new RegisterRequest("Bob Jones", "bob@example.com", "secret123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.type").value("Bearer"))
                .andExpect(jsonPath("$.data.user.name").value("Bob Jones"))
                .andExpect(jsonPath("$.data.user.email").value("bob@example.com"))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_USER"));

        User savedUser = userRepository.findByEmail("bob@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("secret123", savedUser.getPassword()));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register with duplicate email returns 409 Conflict")
    void shouldFailRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest("Bob Jones", "bob@example.com", "secret123");
        userRepository.save(User.builder()
                .name("Bob Jones")
                .email("bob@example.com")
                .password(passwordEncoder.encode("secret123"))
                .role("ROLE_USER")
                .build());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with valid credentials returns 200 OK and JWT token")
    void shouldLoginSuccessfully() throws Exception {
        userRepository.save(User.builder()
                .name("Charlie Brown")
                .email("charlie@example.com")
                .password(passwordEncoder.encode("mypassword"))
                .role("ROLE_USER")
                .build());

        LoginRequest request = new LoginRequest("charlie@example.com", "mypassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("charlie@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login with invalid password returns 401 Unauthorized")
    void shouldFailLoginWithBadPassword() throws Exception {
        userRepository.save(User.builder()
                .name("Charlie Brown")
                .email("charlie@example.com")
                .password(passwordEncoder.encode("mypassword"))
                .role("ROLE_USER")
                .build());

        LoginRequest request = new LoginRequest("charlie@example.com", "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(401));
    }
}
