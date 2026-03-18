package com.foodplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoValidationResponse {
    private boolean valid;
    private double discountAmount;
    private String message;
}
