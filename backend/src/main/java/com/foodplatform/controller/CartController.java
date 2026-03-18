package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.dto.CartRequest;
import com.foodplatform.dto.CartUpdateRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(Authentication authentication) {
        String customerId = authentication.getName();
        Cart cart = cartService.getCart(customerId);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart retrieved"));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Cart>> addToCart(
            @Valid @RequestBody CartRequest request, Authentication authentication) {
        String customerId = authentication.getName();
        Cart cart = cartService.addToCart(customerId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Cart>> updateCartItem(
            @Valid @RequestBody CartUpdateRequest request, Authentication authentication) {
        String customerId = authentication.getName();
        Cart cart = cartService.updateCartItem(customerId, request);
        return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(Authentication authentication) {
        String customerId = authentication.getName();
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.success(null, "Cart cleared"));
    }
}
