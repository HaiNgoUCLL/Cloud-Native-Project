package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.dto.CartRequest;
import com.foodplatform.dto.CartUpdateRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CartController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private CartService cartService;
    @MockBean private JwtUtil jwtUtil;

    private Cart createSampleCart() {
        Cart cart = new Cart();
        cart.setId("cart1");
        cart.setCustomerId("cust1");
        cart.setRestaurantId("rest1");
        cart.setItems(new ArrayList<>(List.of(new Cart.CartItem("m1", "Pizza", 14.99, 2))));
        return cart;
    }

    @Test
    @WithMockUser(username = "cust1")
    void getCart_returns200() throws Exception {
        when(cartService.getCart("cust1")).thenReturn(createSampleCart());

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.restaurantId").value("rest1"));
    }

    @Test
    @WithMockUser(username = "cust1")
    void addToCart_returns200() throws Exception {
        CartRequest request = new CartRequest("rest1", "m1", 1);
        when(cartService.addToCart(eq("cust1"), any(CartRequest.class))).thenReturn(createSampleCart());

        mockMvc.perform(post("/api/cart/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(username = "cust1")
    void updateCartItem_returns200() throws Exception {
        CartUpdateRequest request = new CartUpdateRequest("m1", 3);
        when(cartService.updateCartItem(eq("cust1"), any(CartUpdateRequest.class))).thenReturn(createSampleCart());

        mockMvc.perform(put("/api/cart/update")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "cust1")
    void clearCart_returns200() throws Exception {
        mockMvc.perform(delete("/api/cart/clear").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cart cleared"));
    }
}
