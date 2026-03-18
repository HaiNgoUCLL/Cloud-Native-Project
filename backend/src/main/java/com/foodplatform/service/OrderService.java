package com.foodplatform.service;

import com.foodplatform.dto.OrderRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.model.Order;
import com.foodplatform.model.Restaurant;
import com.foodplatform.model.User;
import com.foodplatform.repository.CartRepository;
import com.foodplatform.repository.OrderRepository;
import com.foodplatform.repository.RestaurantRepository;
import com.foodplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public Order placeOrder(String customerId, OrderRequest request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        List<Order.OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> new Order.OrderItem(ci.getMenuItemId(), ci.getName(), ci.getPrice(), ci.getQuantity()))
                .collect(Collectors.toList());

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(cart.getRestaurantId());
        order.setItems(orderItems);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(totalAmount);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteByCustomerId(customerId);

        return savedOrder;
    }

    public List<Order> getOrders(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return switch (user.getRole()) {
            case CUSTOMER -> orderRepository.findByCustomerIdOrderByCreatedAtDesc(userId);
            case RESTAURANT_OWNER -> {
                List<Restaurant> restaurants = restaurantRepository.findByOwnerId(userId);
                List<String> restaurantIds = restaurants.stream().map(Restaurant::getId).toList();
                yield orderRepository.findAllByOrderByCreatedAtDesc().stream()
                        .filter(o -> restaurantIds.contains(o.getRestaurantId()))
                        .collect(Collectors.toList());
            }
            case DELIVERY_DRIVER -> {
                List<Order> assigned = orderRepository.findByDriverIdOrderByCreatedAtDesc(userId);
                if (assigned.isEmpty()) {
                    yield orderRepository.findByStatusOrderByCreatedAtDesc(Order.OrderStatus.CONFIRMED);
                }
                yield assigned;
            }
            case ADMIN -> orderRepository.findAllByOrderByCreatedAtDesc();
        };
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order updateOrderStatus(String orderId, Order.OrderStatus newStatus, String userId) {
        Order order = getOrderById(orderId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        switch (user.getRole()) {
            case RESTAURANT_OWNER -> {
                if (newStatus != Order.OrderStatus.CONFIRMED && newStatus != Order.OrderStatus.PREPARING) {
                    throw new RuntimeException("Restaurant owner can only confirm or set preparing");
                }
            }
            case DELIVERY_DRIVER -> {
                if (newStatus == Order.OrderStatus.OUT_FOR_DELIVERY) {
                    order.setDriverId(userId);
                } else if (newStatus != Order.OrderStatus.DELIVERED) {
                    throw new RuntimeException("Driver can only set out for delivery or delivered");
                }
            }
            case ADMIN -> { }
            default -> throw new RuntimeException("Not authorized to update order status");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order simulatePayment(String orderId, String customerId) {
        Order order = getOrderById(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Not your order");
        }
        if (order.getPaymentStatus() == Order.PaymentStatus.SIMULATED_PAID) {
            throw new RuntimeException("Order already paid");
        }
        order.setPaymentStatus(Order.PaymentStatus.SIMULATED_PAID);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public Order cancelOrder(String orderId, String customerId) {
        Order order = getOrderById(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Not your order");
        }
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Can only cancel pending orders");
        }
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }
}
