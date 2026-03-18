package com.foodplatform.repository;

import com.foodplatform.model.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {
    List<MenuItem> findByRestaurantId(String restaurantId);
    void deleteByRestaurantId(String restaurantId);
}
