package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.PromotionRedemption;
import com.officefood.healthy_food_api.model.enums.RedemptionStatus;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromotionRedemptionRepository extends UuidJpaRepository<PromotionRedemption> {

    @Query("SELECT COUNT(pr) FROM PromotionRedemption pr " +
           "WHERE pr.order.id = :orderId " +
           "AND pr.promotion.id = :promotionId " +
           "AND pr.status = :status " +
           "AND pr.isActive = true")
    long countByOrderIdAndPromotionIdAndStatus(
        @Param("orderId") String orderId,
        @Param("promotionId") String promotionId,
        @Param("status") RedemptionStatus status
    );
}
