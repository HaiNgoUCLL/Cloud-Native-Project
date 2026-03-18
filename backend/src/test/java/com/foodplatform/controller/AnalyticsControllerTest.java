package com.foodplatform.controller;

import com.foodplatform.dto.RestaurantAnalytics;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AnalyticsController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class AnalyticsControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AnalyticsService analyticsService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "owner1")
    void getAnalytics_withAuth_returns200() throws Exception {
        RestaurantAnalytics analytics = new RestaurantAnalytics();
        analytics.setTotalOrders(25);
        analytics.setTotalRevenue(1250.50);
        analytics.setAverageOrderValue(50.02);
        analytics.setOrdersByStatus(Map.of("DELIVERED", 20, "PENDING", 5));
        analytics.setRevenueByDay(List.of(new RestaurantAnalytics.DayRevenue("03/15", 250.0)));
        analytics.setTopItems(List.of(new RestaurantAnalytics.TopItem("Margherita Pizza", 15, 224.85)));
        analytics.setOrdersByHour(Map.of(12, 5, 18, 8));

        RestaurantAnalytics.ReviewStats reviewStats = new RestaurantAnalytics.ReviewStats();
        reviewStats.setTotalReviews(10);
        reviewStats.setAverageRating(4.3);
        reviewStats.setRatingDistribution(Map.of(5, 4, 4, 3, 3, 2, 2, 1, 1, 0));
        analytics.setReviewStats(reviewStats);

        when(analyticsService.getRestaurantAnalytics("rest1", "owner1")).thenReturn(analytics);

        mockMvc.perform(get("/api/analytics/restaurant/rest1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOrders").value(25))
                .andExpect(jsonPath("$.data.totalRevenue").value(1250.50))
                .andExpect(jsonPath("$.data.reviewStats.totalReviews").value(10));
    }

    @Test
    void getAnalytics_noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/analytics/restaurant/rest1"))
                .andExpect(status().isUnauthorized());
    }
}
