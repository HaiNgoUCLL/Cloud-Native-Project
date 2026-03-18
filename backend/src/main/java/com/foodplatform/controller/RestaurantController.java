package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.model.Restaurant;
import com.foodplatform.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Restaurant>>> getAllRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisine) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants(search, cuisine);
        return ResponseEntity.ok(ApiResponse.success(restaurants, "Restaurants retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Restaurant>> getRestaurantById(@PathVariable String id) {
        Restaurant restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(ApiResponse.success(restaurant, "Restaurant retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Restaurant>> createRestaurant(
            @RequestBody Restaurant restaurant, Authentication authentication) {
        String ownerId = authentication.getName();
        Restaurant created = restaurantService.createRestaurant(restaurant, ownerId);
        return ResponseEntity.ok(ApiResponse.success(created, "Restaurant created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Restaurant>> updateRestaurant(
            @PathVariable String id, @RequestBody Restaurant restaurant, Authentication authentication) {
        String ownerId = authentication.getName();
        Restaurant updated = restaurantService.updateRestaurant(id, restaurant, ownerId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Restaurant updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRestaurant(
            @PathVariable String id, Authentication authentication) {
        String ownerId = authentication.getName();
        restaurantService.deleteRestaurant(id, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Restaurant deleted"));
    }
}
