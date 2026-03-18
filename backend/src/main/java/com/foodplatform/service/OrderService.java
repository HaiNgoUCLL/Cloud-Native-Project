package com.foodplatform.service;

import com.foodplatform.dto.OrderRequest;
import com.foodplatform.model.Cart;
import com.foodplatform.model.NotificationEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PromoCodeService promoCodeService;
    private final CartService cartService;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

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

        double discountAmount = 0;
        String appliedPromo = null;
        if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
            var validation = promoCodeService.validatePromoCode(
                    request.getPromoCode(), totalAmount, cart.getRestaurantId());
            if (validation.isValid()) {
                discountAmount = validation.getDiscountAmount();
                appliedPromo = request.getPromoCode().toUpperCase();
                promoCodeService.applyPromoCode(appliedPromo);
            } else {
                throw new RuntimeException(validation.getMessage());
            }
        }

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setRestaurantId(cart.getRestaurantId());
        order.setItems(orderItems);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(totalAmount - discountAmount);
        order.setPromoCode(appliedPromo);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteByCustomerId(customerId);

        // Notify restaurant owner of new order
        restaurantRepository.findById(savedOrder.getRestaurantId()).ifPresent(restaurant ->
            notificationService.sendNotification(restaurant.getOwnerId(),
                NotificationEvent.newOrder(savedOrder.getId(), "New order #" + savedOrder.getId().substring(savedOrder.getId().length() - 6)))
        );

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
                List<Order> readyOrders = orderRepository.findByStatusOrderByCreatedAtDesc(Order.OrderStatus.READY);
                List<Order> combined = new ArrayList<>(assigned);
                for (Order ro : readyOrders) {
                    if (combined.stream().noneMatch(o -> o.getId().equals(ro.getId()))) {
                        combined.add(ro);
                    }
                }
                yield combined;
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
                if (newStatus == Order.OrderStatus.PICKED_UP) {
                    order.setDriverId(userId);
                } else if (newStatus != Order.OrderStatus.ARRIVED && newStatus != Order.OrderStatus.DELIVERED) {
                    throw new RuntimeException("Driver can only pick up, arrive, or deliver");
                }
            }
            case ADMIN -> { }
            default -> throw new RuntimeException("Not authorized to update order status");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);

        // Notify customer of status change
        String statusText = newStatus.name().replace('_', ' ').toLowerCase();
        notificationService.sendNotification(saved.getCustomerId(),
            NotificationEvent.orderStatusChanged(saved.getId(), "Your order is now " + statusText));

        // If PREPARING, schedule auto-transition to READY after 5 seconds
        if (newStatus == Order.OrderStatus.PREPARING) {
            scheduleReadyTransition(saved.getId());
        }

        // If driver picked up or arrived, notify restaurant owner
        if (newStatus == Order.OrderStatus.PICKED_UP || newStatus == Order.OrderStatus.ARRIVED) {
            String action = newStatus == Order.OrderStatus.PICKED_UP ? "picked up by driver" : "driver has arrived";
            restaurantRepository.findById(saved.getRestaurantId()).ifPresent(restaurant ->
                notificationService.sendNotification(restaurant.getOwnerId(),
                    NotificationEvent.orderAssigned(saved.getId(), "Order #" + saved.getId().substring(saved.getId().length() - 6) + " " + action))
            );
        }

        return saved;
    }

    private void scheduleReadyTransition(String orderId) {
        scheduler.schedule(() -> {
            try {
                Order order = orderRepository.findById(orderId).orElse(null);
                if (order == null || order.getStatus() != Order.OrderStatus.PREPARING) {
                    return;
                }

                order.setStatus(Order.OrderStatus.READY);
                order.setUpdatedAt(LocalDateTime.now());
                orderRepository.save(order);

                // Notify customer
                notificationService.sendNotification(order.getCustomerId(),
                    NotificationEvent.orderStatusChanged(orderId, "Your order is ready for pickup!"));

                // Notify restaurant owner
                restaurantRepository.findById(order.getRestaurantId()).ifPresent(restaurant ->
                    notificationService.sendNotification(restaurant.getOwnerId(),
                        NotificationEvent.orderStatusChanged(orderId, "Order #" + orderId.substring(orderId.length() - 6) + " is now ready"))
                );

                // Notify all drivers
                List<User> drivers = userRepository.findByRole(User.Role.DELIVERY_DRIVER);
                for (User driver : drivers) {
                    notificationService.sendNotification(driver.getId(),
                        NotificationEvent.orderReady(orderId, "Order #" + orderId.substring(orderId.length() - 6) + " is ready for pickup"));
                }
            } catch (Exception e) {
                // Log but don't throw — this runs on a background thread
            }
        }, 5, TimeUnit.SECONDS);
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

    public Cart reorder(String orderId, String customerId) {
        Order order = getOrderById(orderId);
        if (!order.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Not your order");
        }

        Restaurant restaurant = restaurantRepository.findById(order.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant no longer exists"));

        if (!restaurant.isOpen()) {
            throw new RuntimeException("Restaurant is currently closed");
        }

        return cartService.populateFromOrder(customerId, order);
    }
}
