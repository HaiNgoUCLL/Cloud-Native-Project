package com.foodplatform.service;

import com.foodplatform.dto.PromoCodeRequest;
import com.foodplatform.dto.PromoValidationResponse;
import com.foodplatform.model.PromoCode;
import com.foodplatform.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCode createPromoCode(PromoCodeRequest request, String createdBy) {
        if (promoCodeRepository.findByCode(request.getCode().toUpperCase()).isPresent()) {
            throw new RuntimeException("Promo code already exists");
        }

        PromoCode promo = new PromoCode();
        promo.setCode(request.getCode().toUpperCase());
        promo.setDiscountType(PromoCode.DiscountType.valueOf(request.getDiscountType()));
        promo.setDiscountValue(request.getDiscountValue());
        promo.setMinimumOrderAmount(request.getMinimumOrderAmount());
        promo.setMaxUsageCount(request.getMaxUsageCount());
        promo.setCurrentUsageCount(0);
        promo.setActive(true);
        promo.setRestaurantId(request.getRestaurantId());
        promo.setCreatedBy(createdBy);
        promo.setCreatedAt(LocalDateTime.now());

        if (request.getExpiresAt() != null && !request.getExpiresAt().isEmpty()) {
            promo.setExpiresAt(LocalDateTime.parse(request.getExpiresAt()));
        }

        return promoCodeRepository.save(promo);
    }

    public PromoValidationResponse validatePromoCode(String code, double orderAmount, String restaurantId) {
        PromoCode promo = promoCodeRepository.findByCode(code.toUpperCase())
                .orElse(null);

        if (promo == null) {
            return new PromoValidationResponse(false, 0, "Invalid promo code");
        }

        if (!promo.isActive()) {
            return new PromoValidationResponse(false, 0, "Promo code is no longer active");
        }

        if (promo.getExpiresAt() != null && promo.getExpiresAt().isBefore(LocalDateTime.now())) {
            return new PromoValidationResponse(false, 0, "Promo code has expired");
        }

        if (promo.getMaxUsageCount() > 0 && promo.getCurrentUsageCount() >= promo.getMaxUsageCount()) {
            return new PromoValidationResponse(false, 0, "Promo code usage limit reached");
        }

        if (orderAmount < promo.getMinimumOrderAmount()) {
            return new PromoValidationResponse(false, 0,
                    "Minimum order amount is $" + String.format("%.2f", promo.getMinimumOrderAmount()));
        }

        if (promo.getRestaurantId() != null && !promo.getRestaurantId().isEmpty()
                && !promo.getRestaurantId().equals(restaurantId)) {
            return new PromoValidationResponse(false, 0, "Promo code is not valid for this restaurant");
        }

        double discount;
        if (promo.getDiscountType() == PromoCode.DiscountType.PERCENTAGE) {
            discount = Math.round(orderAmount * promo.getDiscountValue() / 100.0 * 100.0) / 100.0;
        } else {
            discount = Math.min(promo.getDiscountValue(), orderAmount);
        }

        return new PromoValidationResponse(true, discount, "Promo code applied! You save $" + String.format("%.2f", discount));
    }

    public void applyPromoCode(String code) {
        promoCodeRepository.findByCode(code.toUpperCase()).ifPresent(promo -> {
            promo.setCurrentUsageCount(promo.getCurrentUsageCount() + 1);
            promoCodeRepository.save(promo);
        });
    }

    public List<PromoCode> getPromoCodesByCreator(String createdBy) {
        return promoCodeRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
    }

    public List<PromoCode> getAllPromoCodes() {
        return promoCodeRepository.findAll();
    }

    public void deactivatePromoCode(String id, String userId) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promo code not found"));
        promo.setActive(false);
        promoCodeRepository.save(promo);
    }
}
