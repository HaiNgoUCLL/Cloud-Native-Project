package com.foodplatform.service;

import com.foodplatform.dto.PromoCodeRequest;
import com.foodplatform.dto.PromoValidationResponse;
import com.foodplatform.model.PromoCode;
import com.foodplatform.repository.PromoCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PromoCodeRepository promoCodeRepository;

    @InjectMocks
    private PromoCodeService promoCodeService;

    private PromoCode activePercentagePromo;
    private PromoCode activeFixedPromo;

    @BeforeEach
    void setUp() {
        activePercentagePromo = new PromoCode();
        activePercentagePromo.setId("promo1");
        activePercentagePromo.setCode("WELCOME10");
        activePercentagePromo.setDiscountType(PromoCode.DiscountType.PERCENTAGE);
        activePercentagePromo.setDiscountValue(10);
        activePercentagePromo.setMinimumOrderAmount(15);
        activePercentagePromo.setMaxUsageCount(100);
        activePercentagePromo.setCurrentUsageCount(0);
        activePercentagePromo.setActive(true);
        activePercentagePromo.setExpiresAt(LocalDateTime.now().plusMonths(3));
        activePercentagePromo.setCreatedBy("admin1");
        activePercentagePromo.setCreatedAt(LocalDateTime.now());

        activeFixedPromo = new PromoCode();
        activeFixedPromo.setId("promo2");
        activeFixedPromo.setCode("SAVE5");
        activeFixedPromo.setDiscountType(PromoCode.DiscountType.FIXED_AMOUNT);
        activeFixedPromo.setDiscountValue(5);
        activeFixedPromo.setMinimumOrderAmount(25);
        activeFixedPromo.setMaxUsageCount(50);
        activeFixedPromo.setCurrentUsageCount(0);
        activeFixedPromo.setActive(true);
        activeFixedPromo.setExpiresAt(LocalDateTime.now().plusMonths(1));
        activeFixedPromo.setCreatedBy("admin1");
        activeFixedPromo.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createPromoCode_success() {
        PromoCodeRequest request = new PromoCodeRequest("NEWCODE", "PERCENTAGE", 15, 20, 50, null, null);
        when(promoCodeRepository.findByCode("NEWCODE")).thenReturn(Optional.empty());
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(i -> i.getArgument(0));

        PromoCode result = promoCodeService.createPromoCode(request, "admin1");

        assertThat(result.getCode()).isEqualTo("NEWCODE");
        assertThat(result.getDiscountType()).isEqualTo(PromoCode.DiscountType.PERCENTAGE);
        assertThat(result.getDiscountValue()).isEqualTo(15);
        assertThat(result.getCurrentUsageCount()).isZero();
        assertThat(result.isActive()).isTrue();
        verify(promoCodeRepository).save(any(PromoCode.class));
    }

    @Test
    void createPromoCode_duplicateCode_throws() {
        PromoCodeRequest request = new PromoCodeRequest("WELCOME10", "PERCENTAGE", 10, 15, 100, null, null);
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        assertThatThrownBy(() -> promoCodeService.createPromoCode(request, "admin1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Promo code already exists");
    }

    @Test
    void validatePromoCode_validPercentage_returnsDiscount() {
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("WELCOME10", 100, null);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualTo(10.0); // 10% of $100
        assertThat(result.getMessage()).contains("You save");
    }

    @Test
    void validatePromoCode_validFixedAmount_returnsDiscount() {
        when(promoCodeRepository.findByCode("SAVE5")).thenReturn(Optional.of(activeFixedPromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("SAVE5", 50, null);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualTo(5.0);
        assertThat(result.getMessage()).contains("You save");
    }

    @Test
    void validatePromoCode_invalidCode_returnsFalse() {
        when(promoCodeRepository.findByCode("DOESNOTEXIST")).thenReturn(Optional.empty());

        PromoValidationResponse result = promoCodeService.validatePromoCode("DOESNOTEXIST", 100, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Invalid promo code");
    }

    @Test
    void validatePromoCode_expired_returnsFalse() {
        activePercentagePromo.setExpiresAt(LocalDateTime.now().minusDays(1));
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("WELCOME10", 100, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Promo code has expired");
    }

    @Test
    void validatePromoCode_maxUsageReached_returnsFalse() {
        activePercentagePromo.setCurrentUsageCount(100);
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("WELCOME10", 100, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Promo code usage limit reached");
    }

    @Test
    void validatePromoCode_belowMinimum_returnsFalse() {
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("WELCOME10", 10, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("Minimum order amount");
    }

    @Test
    void validatePromoCode_wrongRestaurant_returnsFalse() {
        activePercentagePromo.setRestaurantId("restaurant1");
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("WELCOME10", 100, "restaurant2");

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Promo code is not valid for this restaurant");
    }

    @Test
    void validatePromoCode_inactive_returnsFalse() {
        activePercentagePromo.setActive(false);
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("WELCOME10", 100, null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).isEqualTo("Promo code is no longer active");
    }

    @Test
    void validatePromoCode_fixedAmount_cappedAtOrderTotal() {
        activeFixedPromo.setDiscountValue(50);
        activeFixedPromo.setMinimumOrderAmount(0);
        when(promoCodeRepository.findByCode("SAVE5")).thenReturn(Optional.of(activeFixedPromo));

        PromoValidationResponse result = promoCodeService.validatePromoCode("SAVE5", 30, null);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getDiscountAmount()).isEqualTo(30.0); // capped at order total
    }

    @Test
    void applyPromoCode_incrementsUsageCount() {
        when(promoCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(activePercentagePromo));
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(i -> i.getArgument(0));

        promoCodeService.applyPromoCode("WELCOME10");

        verify(promoCodeRepository).save(argThat(promo -> promo.getCurrentUsageCount() == 1));
    }

    @Test
    void deactivatePromoCode_setsInactive() {
        when(promoCodeRepository.findById("promo1")).thenReturn(Optional.of(activePercentagePromo));
        when(promoCodeRepository.save(any(PromoCode.class))).thenAnswer(i -> i.getArgument(0));

        promoCodeService.deactivatePromoCode("promo1", "admin1");

        verify(promoCodeRepository).save(argThat(promo -> !promo.isActive()));
    }
}
