package com.foodplatform.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "promo_codes")
public class PromoCode {
    @Id
    private String id;

    @Indexed(unique = true)
    private String code;

    private DiscountType discountType;
    private double discountValue;
    private double minimumOrderAmount;
    private int maxUsageCount;
    private int currentUsageCount;
    private LocalDateTime expiresAt;
    private boolean isActive;
    private String restaurantId;
    private String createdBy;
    private LocalDateTime createdAt;

    public enum DiscountType {
        PERCENTAGE, FIXED_AMOUNT
    }
}
