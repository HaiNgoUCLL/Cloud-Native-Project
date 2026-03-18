package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.dto.ReviewRequest;
import com.foodplatform.model.Review;
import com.foodplatform.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @Valid @RequestBody ReviewRequest request, Authentication authentication) {
        String customerId = authentication.getName();
        Review review = reviewService.createReview(customerId, request);
        return ResponseEntity.ok(ApiResponse.success(review, "Review submitted successfully"));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByRestaurant(
            @PathVariable String restaurantId) {
        List<Review> reviews = reviewService.getReviewsByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Reviews retrieved"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Review>>> getMyReviews(Authentication authentication) {
        String customerId = authentication.getName();
        List<Review> reviews = reviewService.getReviewsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(reviews, "Your reviews retrieved"));
    }
}
