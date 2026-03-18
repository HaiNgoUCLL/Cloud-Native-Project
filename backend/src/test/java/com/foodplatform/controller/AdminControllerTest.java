package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.model.Order;
import com.foodplatform.model.User;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.AdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AdminController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AdminService adminService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_returns200() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setName("Test");
        user.setRole(User.Role.CUSTOMER);
        when(adminService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getOrders_returns200() throws Exception {
        Order order = new Order();
        order.setId("o1");
        order.setStatus(Order.OrderStatus.PENDING);
        when(adminService.getAllOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRole_returns200() throws Exception {
        User user = new User();
        user.setId("u1");
        user.setName("Test");
        user.setRole(User.Role.ADMIN);
        when(adminService.updateUserRole("u1", User.Role.ADMIN)).thenReturn(user);

        mockMvc.perform(put("/api/admin/users/u1/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("role", "ADMIN"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }
}
