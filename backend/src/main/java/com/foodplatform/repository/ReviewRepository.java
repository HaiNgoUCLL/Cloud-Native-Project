package com.foodplatform.repository;

import com.foodplatform.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId);
    List<Review> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    Optional<Review> findByOrderId(String orderId);
}
