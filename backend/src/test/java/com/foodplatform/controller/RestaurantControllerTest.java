package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.model.Restaurant;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RestaurantController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class RestaurantControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private RestaurantService restaurantService;
    @MockBean private JwtUtil jwtUtil;

    private Restaurant createSampleRestaurant() {
        Restaurant r = new Restaurant();
        r.setId("rest1");
        r.setOwnerId("owner1");
        r.setName("Mario's");
        r.setCuisineType("Italian");
        r.setAddress("123 St");
        r.setOpen(true);
        r.setRating(4.5);
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    @Test
    @WithMockUser
    void getAll_returns200() throws Exception {
        when(restaurantService.getAllRestaurants(null, null)).thenReturn(List.of(createSampleRestaurant()));

        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Mario's"));
    }

    @Test
    @WithMockUser
    void getById_returns200() throws Exception {
        when(restaurantService.getRestaurantById("rest1")).thenReturn(createSampleRestaurant());

        mockMvc.perform(get("/api/restaurants/rest1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Mario's"));
    }

    @Test
    @WithMockUser(username = "owner1")
    void create_returns200() throws Exception {
        Restaurant r = createSampleRestaurant();
        when(restaurantService.createRestaurant(any(Restaurant.class), eq("owner1"))).thenReturn(r);

        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Mario's"));
    }

    @Test
    @WithMockUser(username = "owner1")
    void update_returns200() throws Exception {
        Restaurant r = createSampleRestaurant();
        when(restaurantService.updateRestaurant(eq("rest1"), any(Restaurant.class), eq("owner1"))).thenReturn(r);

        mockMvc.perform(put("/api/restaurants/rest1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(r)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restaurant updated"));
    }
}
