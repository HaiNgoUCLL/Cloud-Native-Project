package com.foodplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantAnalytics {
    private int totalOrders;
    private double totalRevenue;
    private double averageOrderValue;
    private Map<String, Integer> ordersByStatus;
    private List<DayRevenue> revenueByDay;
    private List<TopItem> topItems;
    private Map<Integer, Integer> ordersByHour;
    private ReviewStats reviewStats;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayRevenue {
        private String date;
        private double revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopItem {
        private String name;
        private int quantity;
        private double revenue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewStats {
        private double averageRating;
        private int totalReviews;
        private Map<Integer, Integer> ratingDistribution;
    }
}
