package com.foodplatform.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartUpdateRequest {
    @NotBlank(message = "Menu item ID is required")
    private String menuItemId;

    @Min(value = 0, message = "Quantity must be 0 or more")
    private int quantity;
}
