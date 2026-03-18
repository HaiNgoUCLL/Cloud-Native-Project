package com.foodplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodplatform.dto.OrderRequest;
import com.foodplatform.model.Order;
import com.foodplatform.security.JwtAuthFilter;
import com.foodplatform.security.JwtUtil;
import com.foodplatform.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = OrderController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthFilter.class))
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private OrderService orderService;
    @MockBean private JwtUtil jwtUtil;

    private Order createSampleOrder() {
        Order order = new Order();
        order.setId("order1");
        order.setCustomerId("cust1");
        order.setRestaurantId("rest1");
        order.setItems(List.of(new Order.OrderItem("m1", "Margherita Pizza", 14.99, 2)));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(29.98);
        order.setDeliveryAddress("456 Oak Ave");
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }

    @Test
    @WithMockUser(username = "cust1")
    void placeOrder_returns200() throws Exception {
        OrderRequest request = new OrderRequest("456 Oak Ave", Order.PaymentMethod.CREDIT_CARD, "WELCOME10");
        Order order = createSampleOrder();
        order.setPromoCode("WELCOME10");
        order.setDiscountAmount(3.0);
        order.setTotalAmount(26.98);

        when(orderService.placeOrder(eq("cust1"), any(OrderRequest.class))).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.promoCode").value("WELCOME10"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "cust1")
    void cancelOrder_returns200() throws Exception {
        Order order = createSampleOrder();
        order.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder("order1", "cust1")).thenReturn(order);

        mockMvc.perform(put("/api/orders/order1/cancel").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "owner1")
    void updateOrderStatus_returns200() throws Exception {
        Order order = createSampleOrder();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderService.updateOrderStatus(eq("order1"), eq(Order.OrderStatus.CONFIRMED), eq("owner1")))
                .thenReturn(order);

        mockMvc.perform(put("/api/orders/order1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "CONFIRMED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }
}
