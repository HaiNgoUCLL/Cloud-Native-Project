package com.foodplatform.service;

import com.foodplatform.dto.ReviewRequest;
import com.foodplatform.model.Order;
import com.foodplatform.model.Review;
import com.foodplatform.model.User;
import com.foodplatform.repository.OrderRepository;
import com.foodplatform.repository.RestaurantRepository;
import com.foodplatform.repository.ReviewRepository;
import com.foodplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;

    public Review createReview(String customerId, ReviewRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Not your order");
        }

        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new RuntimeException("Can only review delivered orders");
        }

        if (reviewRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new RuntimeException("You have already reviewed this order");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setCustomerId(customerId);
        review.setCustomerName(customer.getName());
        review.setRestaurantId(order.getRestaurantId());
        review.setOrderId(request.getOrderId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);

        updateRestaurantRating(order.getRestaurantId());

        return saved;
    }

    public List<Review> getReviewsByRestaurant(String restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
    }

    public List<Review> getReviewsByCustomer(String customerId) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    private void updateRestaurantRating(String restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        if (reviews.isEmpty()) return;

        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        final double rounded = Math.round(avg * 10.0) / 10.0;

        restaurantRepository.findById(restaurantId).ifPresent(restaurant -> {
            restaurant.setRating(rounded);
            restaurantRepository.save(restaurant);
        });
    }
}
