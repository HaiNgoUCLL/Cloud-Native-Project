package com.foodplatform.repository;

import com.foodplatform.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    List<Order> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId);
    List<Order> findByDriverIdOrderByCreatedAtDesc(String driverId);
    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
    List<Order> findAllByOrderByCreatedAtDesc();
}
