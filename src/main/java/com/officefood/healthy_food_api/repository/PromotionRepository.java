package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import java.util.Optional;

public interface PromotionRepository extends UuidJpaRepository<Promotion> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
}
