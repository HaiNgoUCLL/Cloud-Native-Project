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
@Document(collection = "restaurants")
public class Restaurant {
    @Id
    private String id;
    private String ownerId;
    private String name;
    private String description;
    private String cuisineType;
    private String address;
    private String imageUrl;
    private boolean isOpen;
    private double rating;
    private LocalDateTime createdAt;
}
