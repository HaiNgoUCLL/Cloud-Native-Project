package com.foodplatform.repository;

import com.foodplatform.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RestaurantRepository extends MongoRepository<Restaurant, String> {
    List<Restaurant> findByOwnerId(String ownerId);
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    List<Restaurant> findByCuisineTypeIgnoreCase(String cuisineType);
    List<Restaurant> findByNameContainingIgnoreCaseAndCuisineTypeIgnoreCase(String name, String cuisineType);
}
