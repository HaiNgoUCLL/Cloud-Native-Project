package com.foodplatform.service;

import com.foodplatform.model.Restaurant;
import com.foodplatform.repository.MenuItemRepository;
import com.foodplatform.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public List<Restaurant> getAllRestaurants(String search, String cuisine) {
        if (search != null && !search.isBlank() && cuisine != null && !cuisine.isBlank()) {
            return restaurantRepository.findByNameContainingIgnoreCaseAndCuisineTypeIgnoreCase(search, cuisine);
        } else if (search != null && !search.isBlank()) {
            return restaurantRepository.findByNameContainingIgnoreCase(search);
        } else if (cuisine != null && !cuisine.isBlank()) {
            return restaurantRepository.findByCuisineTypeIgnoreCase(cuisine);
        }
        return restaurantRepository.findAll();
    }

    public Restaurant getRestaurantById(String id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    public Restaurant createRestaurant(Restaurant restaurant, String ownerId) {
        restaurant.setOwnerId(ownerId);
        restaurant.setCreatedAt(LocalDateTime.now());
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(String id, Restaurant updated, String ownerId) {
        Restaurant restaurant = getRestaurantById(id);
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only update your own restaurant");
        }
        restaurant.setName(updated.getName());
        restaurant.setDescription(updated.getDescription());
        restaurant.setCuisineType(updated.getCuisineType());
        restaurant.setAddress(updated.getAddress());
        restaurant.setImageUrl(updated.getImageUrl());
        restaurant.setOpen(updated.isOpen());
        return restaurantRepository.save(restaurant);
    }

    public void deleteRestaurant(String id, String ownerId) {
        Restaurant restaurant = getRestaurantById(id);
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only delete your own restaurant");
        }
        menuItemRepository.deleteByRestaurantId(id);
        restaurantRepository.deleteById(id);
    }

    public List<Restaurant> getRestaurantsByOwner(String ownerId) {
        return restaurantRepository.findByOwnerId(ownerId);
    }
}
