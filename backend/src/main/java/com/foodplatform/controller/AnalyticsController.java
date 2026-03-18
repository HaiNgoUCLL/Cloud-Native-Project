package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.dto.RestaurantAnalytics;
import com.foodplatform.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantAnalytics>> getRestaurantAnalytics(
            @PathVariable String restaurantId, Authentication authentication) {
        String ownerId = authentication.getName();
        RestaurantAnalytics analytics = analyticsService.getRestaurantAnalytics(restaurantId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics retrieved"));
    }
}
