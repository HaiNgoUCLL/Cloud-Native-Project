package com.foodplatform.service;

import com.foodplatform.dto.RestaurantAnalytics;
import com.foodplatform.model.Order;
import com.foodplatform.model.Restaurant;
import com.foodplatform.model.Review;
import com.foodplatform.repository.OrderRepository;
import com.foodplatform.repository.RestaurantRepository;
import com.foodplatform.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Restaurant restaurant;
    private List<Order> orders;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId("rest1");
        restaurant.setOwnerId("owner1");
        restaurant.setName("Mario's Trattoria");

        LocalDateTime now = LocalDateTime.now();

        Order order1 = new Order();
        order1.setId("o1");
        order1.setRestaurantId("rest1");
        order1.setStatus(Order.OrderStatus.DELIVERED);
        order1.setPaymentStatus(Order.PaymentStatus.SIMULATED_PAID);
        order1.setTotalAmount(45.97);
        order1.setCreatedAt(now.minusDays(2));
        order1.setItems(List.of(
                new Order.OrderItem("m1", "Margherita Pizza", 14.99, 2),
                new Order.OrderItem("m2", "Spaghetti Carbonara", 15.99, 1)
        ));

        Order order2 = new Order();
        order2.setId("o2");
        order2.setRestaurantId("rest1");
        order2.setStatus(Order.OrderStatus.DELIVERED);
        order2.setPaymentStatus(Order.PaymentStatus.SIMULATED_PAID);
        order2.setTotalAmount(26.98);
        order2.setCreatedAt(now.minusDays(1));
        order2.setItems(List.of(
                new Order.OrderItem("m1", "Margherita Pizza", 14.99, 1),
                new Order.OrderItem("m3", "Tiramisu", 9.99, 1)
        ));

        Order order3 = new Order();
        order3.setId("o3");
        order3.setRestaurantId("rest1");
        order3.setStatus(Order.OrderStatus.CANCELLED);
        order3.setPaymentStatus(Order.PaymentStatus.PENDING);
        order3.setTotalAmount(36.99);
        order3.setCreatedAt(now.minusDays(3));
        order3.setItems(List.of(
                new Order.OrderItem("m2", "Spaghetti Carbonara", 15.99, 1)
        ));

        orders = List.of(order1, order2, order3);
    }

    @Test
    void getAnalytics_basicStats_correct() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc("rest1")).thenReturn(List.of());

        RestaurantAnalytics analytics = analyticsService.getRestaurantAnalytics("rest1", "owner1");

        assertThat(analytics.getTotalOrders()).isEqualTo(3);
        assertThat(analytics.getTotalRevenue()).isEqualTo(72.95); // 45.97 + 26.98
        assertThat(analytics.getAverageOrderValue()).isEqualTo(72.95 / 2); // only paid orders
    }

    @Test
    void getAnalytics_ordersByStatus_correct() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc("rest1")).thenReturn(List.of());

        RestaurantAnalytics analytics = analyticsService.getRestaurantAnalytics("rest1", "owner1");

        assertThat(analytics.getOrdersByStatus()).containsEntry("DELIVERED", 2);
        assertThat(analytics.getOrdersByStatus()).containsEntry("CANCELLED", 1);
    }

    @Test
    void getAnalytics_topItems_sorted() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc("rest1")).thenReturn(List.of());

        RestaurantAnalytics analytics = analyticsService.getRestaurantAnalytics("rest1", "owner1");

        assertThat(analytics.getTopItems()).isNotEmpty();
        // Margherita Pizza ordered 3 times total (2 + 1), should be first
        assertThat(analytics.getTopItems().get(0).getName()).isEqualTo("Margherita Pizza");
        assertThat(analytics.getTopItems().get(0).getQuantity()).isEqualTo(3);
    }

    @Test
    void getAnalytics_reviewStats_correct() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);

        Review rev1 = new Review(); rev1.setRating(5);
        Review rev2 = new Review(); rev2.setRating(4);
        Review rev3 = new Review(); rev3.setRating(3);
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc("rest1"))
                .thenReturn(List.of(rev1, rev2, rev3));

        RestaurantAnalytics analytics = analyticsService.getRestaurantAnalytics("rest1", "owner1");

        assertThat(analytics.getReviewStats().getTotalReviews()).isEqualTo(3);
        assertThat(analytics.getReviewStats().getAverageRating()).isEqualTo(4.0);
        assertThat(analytics.getReviewStats().getRatingDistribution()).containsEntry(5, 1);
        assertThat(analytics.getReviewStats().getRatingDistribution()).containsEntry(4, 1);
        assertThat(analytics.getReviewStats().getRatingDistribution()).containsEntry(3, 1);
    }

    @Test
    void getAnalytics_notOwner_throws() {
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));

        assertThatThrownBy(() -> analyticsService.getRestaurantAnalytics("rest1", "wrongOwner"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Restaurant not found or not owned by you");
    }
}
