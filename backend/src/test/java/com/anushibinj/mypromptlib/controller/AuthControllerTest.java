package com.anushibinj.mypromptlib.controller;

import com.anushibinj.mypromptlib.model.User;
import com.anushibinj.mypromptlib.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .username("testuser")
                .password("password")
                .token("test-token")
                .build();
    }

    @Test
    void testRegister_success() throws Exception {
        when(userService.registerUser("testuser", "password")).thenReturn(testUser);

        String requestBody = "{\"username\": \"testuser\", \"password\": \"password\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testRegister_usernameAlreadyExists() throws Exception {
        when(userService.registerUser(anyString(), anyString()))
                .thenThrow(new RuntimeException("Username already exists"));

        String requestBody = "{\"username\": \"testuser\", \"password\": \"password\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username already exists"));
    }

    @Test
    void testRegister_validationFail() throws Exception {
        String requestBody = "{\"username\": \"\", \"password\": \"\"}";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_success() throws Exception {
        when(userService.loginUser("testuser", "password")).thenReturn(Optional.of(testUser));

        String requestBody = "{\"username\": \"testuser\", \"password\": \"password\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testLogin_invalidCredentials() throws Exception {
        when(userService.loginUser("testuser", "wrongpassword")).thenReturn(Optional.empty());

        String requestBody = "{\"username\": \"testuser\", \"password\": \"wrongpassword\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }
}
