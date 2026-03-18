package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.dto.OrderRequest;
import com.foodplatform.dto.StatusUpdateRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.model.Order;
import com.foodplatform.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(
            @Valid @RequestBody OrderRequest request, Authentication authentication) {
        String customerId = authentication.getName();
        Order order = orderService.placeOrder(customerId, request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getOrders(Authentication authentication) {
        String userId = authentication.getName();
        List<Order> orders = orderService.getOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable String id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order, "Order retrieved"));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable String id, @Valid @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        Order order = orderService.updateOrderStatus(id, request.getStatus(), userId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order status updated"));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Order>> simulatePayment(
            @PathVariable String id, Authentication authentication) {
        String customerId = authentication.getName();
        Order order = orderService.simulatePayment(id, customerId);
        return ResponseEntity.ok(ApiResponse.success(order, "Payment simulated successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Order>> cancelOrder(
            @PathVariable String id, Authentication authentication) {
        String customerId = authentication.getName();
        Order order = orderService.cancelOrder(id, customerId);
        return ResponseEntity.ok(ApiResponse.success(order, "Order cancelled"));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<ApiResponse<Cart>> reorder(
            @PathVariable String id, Authentication authentication) {
        String customerId = authentication.getName();
        Cart cart = orderService.reorder(id, customerId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Items added to cart"));
    }
}
