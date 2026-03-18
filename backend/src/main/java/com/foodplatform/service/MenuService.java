package com.foodplatform.service;

import com.foodplatform.model.MenuItem;
import com.foodplatform.model.Restaurant;
import com.foodplatform.repository.MenuItemRepository;
import com.foodplatform.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public List<MenuItem> getMenuByRestaurant(String restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public MenuItem addMenuItem(String restaurantId, MenuItem menuItem, String ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only manage your own restaurant's menu");
        }
        menuItem.setRestaurantId(restaurantId);
        return menuItemRepository.save(menuItem);
    }

    public MenuItem updateMenuItem(String restaurantId, String itemId, MenuItem updated, String ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only manage your own restaurant's menu");
        }
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        item.setName(updated.getName());
        item.setDescription(updated.getDescription());
        item.setPrice(updated.getPrice());
        item.setCategory(updated.getCategory());
        item.setImageUrl(updated.getImageUrl());
        item.setAvailable(updated.isAvailable());
        return menuItemRepository.save(item);
    }

    public void deleteMenuItem(String restaurantId, String itemId, String ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
        if (!restaurant.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("You can only manage your own restaurant's menu");
        }
        menuItemRepository.deleteById(itemId);
    }
}
