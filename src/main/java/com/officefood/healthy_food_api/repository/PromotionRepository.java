package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Promotion;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface PromotionRepository extends JpaRepository<Promotion, java.util.UUID>, PromotionRepositoryCustom {
    Optional<Promotion> findByCodeAndIsActiveTrue(String code);

    @Query("select p from Promotion p where p.code = :code and p.isActive = true and (p.startsAt is null or p.startsAt <= CURRENT_TIMESTAMP) and (p.endsAt is null or p.endsAt >= CURRENT_TIMESTAMP)")
    Optional<Promotion> findActiveByCode(@Param("code") String code);

    @Query("select p from Promotion p where p.isActive = true " +
           "and (p.startsAt is null or p.startsAt <= CURRENT_TIMESTAMP) " +
           "and (p.endsAt is null or p.endsAt >= CURRENT_TIMESTAMP) " +
           "and (p.minOrderValue is null or :subtotal >= p.minOrderValue)")
    List<Promotion> findApplicable(@Param("subtotal") double orderSubtotal);
}
