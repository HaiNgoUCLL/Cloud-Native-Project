package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.dto.AuthRequest;
import com.foodplatform.dto.AuthResponse;
import com.foodplatform.dto.RegisterRequest;
import com.foodplatform.model.User;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void register_returns200() throws Exception {
        RegisterRequest request = new RegisterRequest("John", "john@test.com", "pass123", User.Role.CUSTOMER, "123 St", "555-1234");
        AuthResponse response = new AuthResponse("token123", "u1", "John", "john@test.com", User.Role.CUSTOMER);
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("token123"));
    }

    @Test
    @WithMockUser
    void login_returns200() throws Exception {
        AuthRequest request = new AuthRequest("john@test.com", "pass123");
        AuthResponse response = new AuthResponse("token123", "u1", "John", "john@test.com", User.Role.CUSTOMER);
        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("token123"));
    }

    @Test
    @WithMockUser(username = "u1")
    void getCurrentUser_returns200() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setName("John");
        user.setEmail("john@test.com");
        user.setRole(User.Role.CUSTOMER);
        when(authService.getCurrentUser("u1")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("John"));
    }
}
