package com.foodplatform.service;

import com.foodplatform.dto.OrderRequest;
import com.foodplatform.dto.PromoValidationResponse;
import com.foodplatform.model.*;
import com.foodplatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private PromoCodeService promoCodeService;
    @Mock private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private Cart cart;
    private OrderRequest orderRequest;
    private User customer;
    private User owner;
    private User driver;
    private Restaurant restaurant;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId("cart1");
        cart.setCustomerId("cust1");
        cart.setRestaurantId("rest1");
        cart.setItems(new ArrayList<>(List.of(
                new Cart.CartItem("m1", "Margherita Pizza", 14.99, 2),
                new Cart.CartItem("m2", "Carbonara", 15.99, 1)
        )));

        orderRequest = new OrderRequest("456 Oak Ave", Order.PaymentMethod.CREDIT_CARD, null);

        customer = new User();
        customer.setId("cust1");
        customer.setName("John");
        customer.setRole(User.Role.CUSTOMER);

        owner = new User();
        owner.setId("owner1");
        owner.setName("Mario");
        owner.setRole(User.Role.RESTAURANT_OWNER);

        driver = new User();
        driver.setId("driver1");
        driver.setName("Mike");
        driver.setRole(User.Role.DELIVERY_DRIVER);

        restaurant = new Restaurant();
        restaurant.setId("rest1");
        restaurant.setOwnerId("owner1");
        restaurant.setName("Mario's Trattoria");

        pendingOrder = new Order();
        pendingOrder.setId("order1");
        pendingOrder.setCustomerId("cust1");
        pendingOrder.setRestaurantId("rest1");
        pendingOrder.setStatus(Order.OrderStatus.PENDING);
        pendingOrder.setTotalAmount(45.97);
        pendingOrder.setPaymentStatus(Order.PaymentStatus.PENDING);
        pendingOrder.setItems(List.of(new Order.OrderItem("m1", "Margherita Pizza", 14.99, 2)));
        pendingOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void placeOrder_success_createsOrder() {
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId("newOrder1");
            return o;
        });
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        Order result = orderService.placeOrder("cust1", orderRequest);

        assertThat(result.getCustomerId()).isEqualTo("cust1");
        assertThat(result.getRestaurantId()).isEqualTo("rest1");
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualTo(45.97); // 14.99*2 + 15.99
        assertThat(result.getDeliveryAddress()).isEqualTo("456 Oak Ave");
        verify(cartRepository).deleteByCustomerId("cust1");
        verify(notificationService).sendNotification(eq("owner1"), any(NotificationEvent.class));
    }

    @Test
    void placeOrder_emptyCart_throws() {
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder("cust1", orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cart is empty");
    }

    @Test
    void placeOrder_withValidPromo_appliesDiscount() {
        OrderRequest requestWithPromo = new OrderRequest("456 Oak Ave", Order.PaymentMethod.CREDIT_CARD, "WELCOME10");
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(cart));
        when(promoCodeService.validatePromoCode("WELCOME10", 45.97, "rest1"))
                .thenReturn(new PromoValidationResponse(true, 4.60, "Promo applied!"));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId("newOrder1");
            return o;
        });
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        Order result = orderService.placeOrder("cust1", requestWithPromo);

        assertThat(result.getPromoCode()).isEqualTo("WELCOME10");
        assertThat(result.getDiscountAmount()).isEqualTo(4.60);
        assertThat(result.getTotalAmount()).isEqualTo(41.37); // 45.97 - 4.60
        verify(promoCodeService).applyPromoCode("WELCOME10");
    }

    @Test
    void placeOrder_withInvalidPromo_throws() {
        OrderRequest requestWithPromo = new OrderRequest("456 Oak Ave", Order.PaymentMethod.CREDIT_CARD, "BADCODE");
        when(cartRepository.findByCustomerId("cust1")).thenReturn(Optional.of(cart));
        when(promoCodeService.validatePromoCode("BADCODE", 45.97, "rest1"))
                .thenReturn(new PromoValidationResponse(false, 0, "Invalid promo code"));

        assertThatThrownBy(() -> orderService.placeOrder("cust1", requestWithPromo))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid promo code");
    }

    @Test
    void updateStatus_ownerConfirm_success() {
        when(orderRepository.findById("order1")).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findById("owner1")).thenReturn(Optional.of(owner));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.updateOrderStatus("order1", Order.OrderStatus.CONFIRMED, "owner1");

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
        verify(notificationService).sendNotification(eq("cust1"), any(NotificationEvent.class));
    }

    @Test
    void updateStatus_ownerInvalidStatus_throws() {
        when(orderRepository.findById("order1")).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findById("owner1")).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> orderService.updateOrderStatus("order1", Order.OrderStatus.DELIVERED, "owner1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Restaurant owner can only confirm or set preparing");
    }

    @Test
    void updateStatus_driverOutForDelivery_setsDriverId() {
        pendingOrder.setStatus(Order.OrderStatus.PREPARING);
        when(orderRepository.findById("order1")).thenReturn(Optional.of(pendingOrder));
        when(userRepository.findById("driver1")).thenReturn(Optional.of(driver));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        Order result = orderService.updateOrderStatus("order1", Order.OrderStatus.OUT_FOR_DELIVERY, "driver1");

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.OUT_FOR_DELIVERY);
        assertThat(result.getDriverId()).isEqualTo("driver1");
    }

    @Test
    void cancelOrder_pending_success() {
        when(orderRepository.findById("order1")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.cancelOrder("order1", "cust1");

        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_notPending_throws() {
        pendingOrder.setStatus(Order.OrderStatus.CONFIRMED);
        when(orderRepository.findById("order1")).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> orderService.cancelOrder("order1", "cust1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Can only cancel pending orders");
    }

    @Test
    void simulatePayment_success() {
        when(orderRepository.findById("order1")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        Order result = orderService.simulatePayment("order1", "cust1");

        assertThat(result.getPaymentStatus()).isEqualTo(Order.PaymentStatus.SIMULATED_PAID);
    }
}
