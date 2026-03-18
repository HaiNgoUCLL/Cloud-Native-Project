package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.model.Order;
import com.foodplatform.model.User;
import com.foodplatform.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        users.forEach(u -> u.setPasswordHash(null));
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved"));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = adminService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved"));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable String id, @RequestBody Map<String, String> body) {
        User.Role newRole = User.Role.valueOf(body.get("role"));
        User user = adminService.updateUserRole(id, newRole);
        user.setPasswordHash(null);
        return ResponseEntity.ok(ApiResponse.success(user, "User role updated"));
    }
}
