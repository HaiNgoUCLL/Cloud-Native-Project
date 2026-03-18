package com.foodplatform.service;

import com.foodplatform.dto.CartRequest;
import com.foodplatform.dto.CartUpdateRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.model.MenuItem;
import com.foodplatform.repository.CartRepository;
import com.foodplatform.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;

    public Cart getCart(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setCustomerId(customerId);
                    cart.setItems(new ArrayList<>());
                    return cart;
                });
    }

    public Cart addToCart(String customerId, CartRequest request) {
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        Cart cart = cartRepository.findByCustomerId(customerId).orElse(null);

        if (cart == null) {
            cart = new Cart();
            cart.setCustomerId(customerId);
            cart.setRestaurantId(request.getRestaurantId());
            cart.setItems(new ArrayList<>());
        } else if (cart.getRestaurantId() != null &&
                !cart.getRestaurantId().equals(request.getRestaurantId())) {
            cart.setRestaurantId(request.getRestaurantId());
            cart.setItems(new ArrayList<>());
        }

        Optional<Cart.CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + request.getQuantity());
        } else {
            Cart.CartItem cartItem = new Cart.CartItem();
            cartItem.setMenuItemId(menuItem.getId());
            cartItem.setName(menuItem.getName());
            cartItem.setPrice(menuItem.getPrice());
            cartItem.setQuantity(request.getQuantity());
            cart.getItems().add(cartItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateCartItem(String customerId, CartUpdateRequest request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (request.getQuantity() == 0) {
            cart.getItems().removeIf(item -> item.getMenuItemId().equals(request.getMenuItemId()));
        } else {
            cart.getItems().stream()
                    .filter(item -> item.getMenuItemId().equals(request.getMenuItemId()))
                    .findFirst()
                    .ifPresent(item -> item.setQuantity(request.getQuantity()));
        }

        if (cart.getItems().isEmpty()) {
            cartRepository.deleteById(cart.getId());
            Cart emptyCart = new Cart();
            emptyCart.setCustomerId(customerId);
            emptyCart.setItems(new ArrayList<>());
            return emptyCart;
        }

        return cartRepository.save(cart);
    }

    public void clearCart(String customerId) {
        cartRepository.deleteByCustomerId(customerId);
    }
}
