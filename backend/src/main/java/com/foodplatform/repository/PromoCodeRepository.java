package com.foodplatform.repository;

import com.foodplatform.model.PromoCode;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PromoCodeRepository extends MongoRepository<PromoCode, String> {
    Optional<PromoCode> findByCode(String code);
    List<PromoCode> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    List<PromoCode> findByRestaurantIdOrderByCreatedAtDesc(String restaurantId);
}
