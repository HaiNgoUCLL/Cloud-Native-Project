package com.foodplatform.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    @Test
    void orderStatus_has8Values() {
        Order.OrderStatus[] values = Order.OrderStatus.values();
        assertThat(values).hasSize(8);
        assertThat(values).contains(
                Order.OrderStatus.PENDING, Order.OrderStatus.CONFIRMED,
                Order.OrderStatus.PREPARING, Order.OrderStatus.READY,
                Order.OrderStatus.PICKED_UP, Order.OrderStatus.ARRIVED,
                Order.OrderStatus.DELIVERED, Order.OrderStatus.CANCELLED
        );
    }

    @Test
    void orderItem_creation() {
        Order.OrderItem item = new Order.OrderItem("m1", "Pizza", 14.99, 2);
        assertThat(item.getMenuItemId()).isEqualTo("m1");
        assertThat(item.getName()).isEqualTo("Pizza");
        assertThat(item.getPrice()).isEqualTo(14.99);
        assertThat(item.getQuantity()).isEqualTo(2);
    }

    @Test
    void paymentMethod_hasAllValues() {
        Order.PaymentMethod[] values = Order.PaymentMethod.values();
        assertThat(values).hasSize(3);
        assertThat(values).contains(
                Order.PaymentMethod.CREDIT_CARD,
                Order.PaymentMethod.PAYPAL,
                Order.PaymentMethod.CASH
        );
    }
}
