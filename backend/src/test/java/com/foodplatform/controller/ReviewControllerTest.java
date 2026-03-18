package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.dto.ReviewRequest;
import com.foodplatform.model.Review;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.ReviewService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ReviewController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ReviewService reviewService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "cust1")
    void createReview_returns200() throws Exception {
        ReviewRequest request = new ReviewRequest("order1", 5, "Amazing food!");
        Review review = new Review("rev1", "cust1", "John", "rest1", "order1", 5, "Amazing food!", 0.95, LocalDateTime.now());
        when(reviewService.createReview(eq("cust1"), any(ReviewRequest.class))).thenReturn(review);

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.comment").value("Amazing food!"));
    }

    @Test
    @WithMockUser
    void getReviewsByRestaurant_publicAccess_returns200() throws Exception {
        Review review = new Review("rev1", "cust1", "John", "rest1", "order1", 5, "Great!", null, LocalDateTime.now());
        when(reviewService.getReviewsByRestaurant("rest1")).thenReturn(List.of(review));

        mockMvc.perform(get("/api/reviews/restaurant/rest1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].rating").value(5));
    }

    @Test
    @WithMockUser(username = "cust1")
    void getMyReviews_authenticated_returns200() throws Exception {
        Review review = new Review("rev1", "cust1", "John", "rest1", "order1", 4, "Good!", null, LocalDateTime.now());
        when(reviewService.getReviewsByCustomer("cust1")).thenReturn(List.of(review));

        mockMvc.perform(get("/api/reviews/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].customerName").value("John"));
    }
}
