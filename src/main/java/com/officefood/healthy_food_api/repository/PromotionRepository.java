package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface PromotionRepository extends UuidJpaRepository<Promotion>, JpaSpecificationExecutor<Promotion> {
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);
}
