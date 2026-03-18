package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.model.MenuItem;
import com.foodplatform.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItem>>> getMenu(@PathVariable String restaurantId) {
        List<MenuItem> menu = menuService.getMenuByRestaurant(restaurantId);
        return ResponseEntity.ok(ApiResponse.success(menu, "Menu retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItem>> addMenuItem(
            @PathVariable String restaurantId, @RequestBody MenuItem menuItem,
            Authentication authentication) {
        String ownerId = authentication.getName();
        MenuItem created = menuService.addMenuItem(restaurantId, menuItem, ownerId);
        return ResponseEntity.ok(ApiResponse.success(created, "Menu item added"));
    }

    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MenuItem>> updateMenuItem(
            @PathVariable String restaurantId, @PathVariable String itemId,
            @RequestBody MenuItem menuItem, Authentication authentication) {
        String ownerId = authentication.getName();
        MenuItem updated = menuService.updateMenuItem(restaurantId, itemId, menuItem, ownerId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Menu item updated"));
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<String>> deleteMenuItem(
            @PathVariable String restaurantId, @PathVariable String itemId,
            Authentication authentication) {
        String ownerId = authentication.getName();
        menuService.deleteMenuItem(restaurantId, itemId, ownerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu item deleted"));
    }
}
