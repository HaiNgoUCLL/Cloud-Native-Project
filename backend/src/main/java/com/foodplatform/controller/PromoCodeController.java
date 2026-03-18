package com.foodplatform.controller;

import com.foodplatform.dto.ApiResponse;
import com.foodplatform.dto.PromoCodeRequest;
import com.foodplatform.dto.PromoValidationResponse;
import com.foodplatform.model.PromoCode;
import com.foodplatform.service.PromoCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromoCode>> createPromoCode(
            @Valid @RequestBody PromoCodeRequest request, Authentication authentication) {
        String userId = authentication.getName();
        PromoCode promo = promoCodeService.createPromoCode(request, userId);
        return ResponseEntity.ok(ApiResponse.success(promo, "Promo code created"));
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<PromoValidationResponse>> validatePromoCode(
            @RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        double orderAmount = ((Number) body.get("orderAmount")).doubleValue();
        String restaurantId = (String) body.get("restaurantId");
        PromoValidationResponse result = promoCodeService.validatePromoCode(code, orderAmount, restaurantId);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromoCode>>> getPromoCodes(Authentication authentication) {
        String userId = authentication.getName();
        List<PromoCode> codes = promoCodeService.getPromoCodesByCreator(userId);
        return ResponseEntity.ok(ApiResponse.success(codes, "Promo codes retrieved"));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<PromoCode>>> getAllPromoCodes() {
        List<PromoCode> codes = promoCodeService.getAllPromoCodes();
        return ResponseEntity.ok(ApiResponse.success(codes, "All promo codes retrieved"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivatePromoCode(
            @PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        promoCodeService.deactivatePromoCode(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Deactivated", "Promo code deactivated"));
    }
}
