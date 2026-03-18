package com.foodplatform.service;

import com.foodplatform.dto.CartRequest;
import com.foodplatform.dto.CartUpdateRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.model.MenuItem;
import com.foodplatform.model.Order;
import com.foodplatform.repository.CartRepository;
import com.foodplatform.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private MenuItemRepository menuItemRepository;

    @InjectMocks
    private CartService cartService;

    private MenuItem menuItem;
    private Cart existingCart;

    @BeforeEach
    void setUp() {
        menuItem = new MenuItem();
        menuItem.setId("m1");
        menuItem.setName("Margherita Pizza");
        menuItem.setPrice(14.99);
        menuItem.setAvailable(true);

        existingCart = new Cart();
        existingCart.setId("cart1");
        existingCart.setCustomerId("cust1");
        existingCart.setRestaurantId("rest1");
        existingCart.setItems(new ArrayList<>(List.of(
                new Cart.CartItem("m1", "Margherita Pizza", 14.99, 2)
        )));
    }

    @Test
    void getCart_existing_returnsCart() {
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(existingCart));

        Cart result = cartService.getCart("cust1");

        assertThat(result.getId()).isEqualTo("cart1");
        assertThat(result.getItems()).hasSize(1);
    }

    @Test
    void getCart_empty_returnsNewCart() {
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.empty());

        Cart result = cartService.getCart("cust1");

        assertThat(result.getCustomerId()).isEqualTo("cust1");
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void addToCart_newCart_createsCart() {
        CartRequest request = new CartRequest("rest1", "m1", 1);
        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(menuItem));
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        Cart result = cartService.addToCart("cust1", request);

        assertThat(result.getRestaurantId()).isEqualTo("rest1");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Margherita Pizza");
    }

    @Test
    void addToCart_existingItem_incrementsQuantity() {
        CartRequest request = new CartRequest("rest1", "m1", 3);
        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(menuItem));
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        Cart result = cartService.addToCart("cust1", request);

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(5); // 2 + 3
    }

    @Test
    void addToCart_differentRestaurant_clearsCart() {
        CartRequest request = new CartRequest("rest2", "m1", 1);
        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(menuItem));
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        Cart result = cartService.addToCart("cust1", request);

        assertThat(result.getRestaurantId()).isEqualTo("rest2");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    void updateCartItem_removeAtZero_removesItem() {
        CartUpdateRequest request = new CartUpdateRequest("m1", 0);
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(existingCart));

        Cart result = cartService.updateCartItem("cust1", request);

        assertThat(result.getItems()).isEmpty();
        verify(cartRepository).deleteById("cart1");
    }

    @Test
    void clearCart_deletesCart() {
        cartService.clearCart("cust1");
        verify(cartRepository).deleteByCustomerId("cust1");
    }

    @Test
    void populateFromOrder_success() {
        Order order = new Order();
        order.setRestaurantId("rest1");
        order.setItems(List.of(new Order.OrderItem("m1", "Margherita Pizza", 14.99, 2)));

        when(menuItemRepository.findById("m1")).thenReturn(Optional.of(menuItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        Cart result = cartService.populateFromOrder("cust1", order);

        assertThat(result.getRestaurantId()).isEqualTo("rest1");
        assertThat(result.getItems()).hasSize(1);
        verify(cartRepository).deleteByCustomerId("cust1");
    }
}
