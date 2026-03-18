package com.foodplatform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    private String customerId;
    private String restaurantId;
    private List<OrderItem> items;
    private OrderStatus status;
    private double totalAmount;
    private String deliveryAddress;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String driverId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem {
        private String menuItemId;
        private String name;
        private double price;
        private int quantity;
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }

    public enum PaymentStatus {
        PENDING, SIMULATED_PAID, FAILED
    }

    public enum PaymentMethod {
        CREDIT_CARD, PAYPAL, CASH
    }
}
