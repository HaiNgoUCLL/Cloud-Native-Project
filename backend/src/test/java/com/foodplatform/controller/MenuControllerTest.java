package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.model.MenuItem;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.MenuService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = MenuController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class MenuControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MenuService menuService;
    @MockBean private JwtUtil jwtUtil;

    private MenuItem createSampleItem() {
        MenuItem item = new MenuItem();
        item.setId("item1");
        item.setRestaurantId("rest1");
        item.setName("Pizza");
        item.setPrice(14.99);
        item.setCategory("Main");
        item.setAvailable(true);
        return item;
    }

    @Test
    @WithMockUser
    void getMenu_returns200() throws Exception {
        when(menuService.getMenuByRestaurant("rest1")).thenReturn(List.of(createSampleItem()));

        mockMvc.perform(get("/api/restaurants/rest1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Pizza"));
    }

    @Test
    @WithMockUser(username = "owner1")
    void addItem_returns200() throws Exception {
        MenuItem item = createSampleItem();
        when(menuService.addMenuItem(eq("rest1"), any(MenuItem.class), eq("owner1"))).thenReturn(item);

        mockMvc.perform(post("/api/restaurants/rest1/menu")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Pizza"));
    }

    @Test
    @WithMockUser(username = "owner1")
    void updateItem_returns200() throws Exception {
        MenuItem item = createSampleItem();
        when(menuService.updateMenuItem(eq("rest1"), eq("item1"), any(MenuItem.class), eq("owner1"))).thenReturn(item);

        mockMvc.perform(put("/api/restaurants/rest1/menu/item1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Pizza"));
    }
}
