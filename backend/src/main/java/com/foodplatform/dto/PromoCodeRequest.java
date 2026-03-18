package com.foodplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeRequest {
    @NotBlank(message = "Code is required")
    private String code;

    private String discountType;

    @Positive(message = "Discount value must be positive")
    private double discountValue;

    private double minimumOrderAmount;
    private int maxUsageCount;
    private String expiresAt;
    private String restaurantId;
}
