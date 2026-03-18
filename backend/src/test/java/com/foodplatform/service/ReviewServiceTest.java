package com.foodplatform.service;

import com.foodplatform.dto.ReviewRequest;
import com.foodplatform.model.Order;
import com.foodplatform.model.Restaurant;
import com.foodplatform.model.Review;
import com.foodplatform.model.User;
import com.foodplatform.repository.OrderRepository;
import com.foodplatform.repository.RestaurantRepository;
import com.foodplatform.repository.ReviewRepository;
import com.foodplatform.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Order deliveredOrder;
    private User customer;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        customer = new User();
        customer.setId("cust1");
        customer.setName("John Customer");
        customer.setEmail("customer@food.com");
        customer.setRole(User.Role.CUSTOMER);

        restaurant = new Restaurant();
        restaurant.setId("rest1");
        restaurant.setOwnerId("owner1");
        restaurant.setName("Mario's Trattoria");
        restaurant.setRating(4.5);

        deliveredOrder = new Order();
        deliveredOrder.setId("order1");
        deliveredOrder.setCustomerId("cust1");
        deliveredOrder.setRestaurantId("rest1");
        deliveredOrder.setStatus(Order.OrderStatus.DELIVERED);
        deliveredOrder.setCreatedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void createReview_success_updatesRating() {
        ReviewRequest request = new ReviewRequest("order1", 5, "Amazing food!");
        when(orderRepository.findById("order1")).thenReturn(Optional.of(deliveredOrder));
        when(reviewRepository.findByOrderId("order1")).thenReturn(Optional.empty());
        when(userRepository.findById("cust1")).thenReturn(Optional.of(customer));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = i.getArgument(0);
            r.setId("rev1");
            return r;
        });

        // For rating update
        Review savedReview = new Review();
        savedReview.setRating(5);
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc("rest1"))
                .thenReturn(List.of(savedReview));
        when(restaurantRepository.findById("rest1")).thenReturn(Optional.of(restaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        Review result = reviewService.createReview("cust1", request);

        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Amazing food!");
        assertThat(result.getCustomerName()).isEqualTo("John Customer");
        assertThat(result.getRestaurantId()).isEqualTo("rest1");
        verify(restaurantRepository).save(argThat(r -> r.getRating() == 5.0));
    }

    @Test
    void createReview_notYourOrder_throws() {
        ReviewRequest request = new ReviewRequest("order1", 4, "Good");
        when(orderRepository.findById("order1")).thenReturn(Optional.of(deliveredOrder));

        assertThatThrownBy(() -> reviewService.createReview("otherCustomer", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Not your order");
    }

    @Test
    void createReview_notDelivered_throws() {
        deliveredOrder.setStatus(Order.OrderStatus.PREPARING);
        ReviewRequest request = new ReviewRequest("order1", 4, "Good");
        when(orderRepository.findById("order1")).thenReturn(Optional.of(deliveredOrder));

        assertThatThrownBy(() -> reviewService.createReview("cust1", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Can only review delivered orders");
    }

    @Test
    void createReview_alreadyReviewed_throws() {
        ReviewRequest request = new ReviewRequest("order1", 4, "Good");
        when(orderRepository.findById("order1")).thenReturn(Optional.of(deliveredOrder));
        when(reviewRepository.findByOrderId("order1")).thenReturn(Optional.of(new Review()));

        assertThatThrownBy(() -> reviewService.createReview("cust1", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("You have already reviewed this order");
    }

    @Test
    void getReviewsByRestaurant_returnsList() {
        Review r1 = new Review(); r1.setRating(5);
        Review r2 = new Review(); r2.setRating(3);
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc("rest1"))
                .thenReturn(List.of(r1, r2));

        List<Review> result = reviewService.getReviewsByRestaurant("rest1");

        assertThat(result).hasSize(2);
    }

    @Test
    void getReviewsByCustomer_returnsList() {
        Review r1 = new Review(); r1.setCustomerId("cust1");
        when(reviewRepository.findByCustomerIdOrderByCreatedAtDesc("cust1"))
                .thenReturn(List.of(r1));

        List<Review> result = reviewService.getReviewsByCustomer("cust1");

        assertThat(result).hasSize(1);
    }
}
