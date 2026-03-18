package com.foodplatform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "reviews")
public class Review {
    @Id
    private String id;
    private String customerId;
    private String customerName;
    private String restaurantId;
    private String orderId;
    private int rating;
    private String comment;
    private Double sentimentScore;
    private LocalDateTime createdAt;
}
