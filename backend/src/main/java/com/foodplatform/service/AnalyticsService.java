package com.foodplatform.service;

import com.foodplatform.dto.RestaurantAnalytics;
import com.foodplatform.model.Order;
import com.foodplatform.model.Review;
import com.foodplatform.repository.OrderRepository;
import com.foodplatform.repository.RestaurantRepository;
import com.foodplatform.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    public RestaurantAnalytics getRestaurantAnalytics(String restaurantId, String ownerId) {
        restaurantRepository.findById(restaurantId)
                .filter(r -> r.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new RuntimeException("Restaurant not found or not owned by you"));

        List<Order> allOrders = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(o -> restaurantId.equals(o.getRestaurantId()))
                .collect(Collectors.toList());

        List<Order> paidOrders = allOrders.stream()
                .filter(o -> o.getPaymentStatus() == Order.PaymentStatus.SIMULATED_PAID)
                .collect(Collectors.toList());

        RestaurantAnalytics analytics = new RestaurantAnalytics();

        // Basic stats
        analytics.setTotalOrders(allOrders.size());
        analytics.setTotalRevenue(paidOrders.stream().mapToDouble(Order::getTotalAmount).sum());
        analytics.setAverageOrderValue(paidOrders.isEmpty() ? 0 :
                analytics.getTotalRevenue() / paidOrders.size());

        // Orders by status
        Map<String, Integer> ordersByStatus = new LinkedHashMap<>();
        for (Order.OrderStatus status : Order.OrderStatus.values()) {
            int count = (int) allOrders.stream().filter(o -> o.getStatus() == status).count();
            if (count > 0) ordersByStatus.put(status.name(), count);
        }
        analytics.setOrdersByStatus(ordersByStatus);

        // Revenue by day (last 30 days)
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd");
        Map<String, Double> revenueMap = new LinkedHashMap<>();
        for (int i = 0; i < 30; i++) {
            revenueMap.put(thirtyDaysAgo.plusDays(i).format(fmt), 0.0);
        }
        for (Order order : paidOrders) {
            if (order.getCreatedAt() != null) {
                LocalDate orderDate = order.getCreatedAt().toLocalDate();
                if (!orderDate.isBefore(thirtyDaysAgo)) {
                    String key = orderDate.format(fmt);
                    revenueMap.merge(key, order.getTotalAmount(), Double::sum);
                }
            }
        }
        List<RestaurantAnalytics.DayRevenue> revenueByDay = revenueMap.entrySet().stream()
                .map(e -> new RestaurantAnalytics.DayRevenue(e.getKey(), Math.round(e.getValue() * 100.0) / 100.0))
                .collect(Collectors.toList());
        analytics.setRevenueByDay(revenueByDay);

        // Top items
        Map<String, int[]> itemStats = new LinkedHashMap<>(); // [quantity, revenue*100]
        for (Order order : paidOrders) {
            for (Order.OrderItem item : order.getItems()) {
                itemStats.computeIfAbsent(item.getName(), k -> new int[2]);
                int[] stats = itemStats.get(item.getName());
                stats[0] += item.getQuantity();
                stats[1] += (int) (item.getPrice() * item.getQuantity() * 100);
            }
        }
        List<RestaurantAnalytics.TopItem> topItems = itemStats.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue()[0], a.getValue()[0]))
                .limit(10)
                .map(e -> new RestaurantAnalytics.TopItem(e.getKey(), e.getValue()[0], e.getValue()[1] / 100.0))
                .collect(Collectors.toList());
        analytics.setTopItems(topItems);

        // Orders by hour
        Map<Integer, Integer> ordersByHour = new TreeMap<>();
        for (int h = 0; h < 24; h++) ordersByHour.put(h, 0);
        for (Order order : allOrders) {
            if (order.getCreatedAt() != null) {
                int hour = order.getCreatedAt().getHour();
                ordersByHour.merge(hour, 1, Integer::sum);
            }
        }
        analytics.setOrdersByHour(ordersByHour);

        // Review stats
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);
        RestaurantAnalytics.ReviewStats reviewStats = new RestaurantAnalytics.ReviewStats();
        reviewStats.setTotalReviews(reviews.size());
        reviewStats.setAverageRating(reviews.isEmpty() ? 0 :
                Math.round(reviews.stream().mapToInt(Review::getRating).average().orElse(0) * 10.0) / 10.0);
        Map<Integer, Integer> ratingDist = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ratingDist.put(i, (int) reviews.stream().filter(r -> r.getRating() == rating).count());
        }
        reviewStats.setRatingDistribution(ratingDist);
        analytics.setReviewStats(reviewStats);

        return analytics;
    }
}
