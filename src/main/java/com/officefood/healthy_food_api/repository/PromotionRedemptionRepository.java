package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.PromotionRedemption;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, java.util.UUID>, PromotionRedemptionRepositoryCustom {
    int countByPromotionId(java.util.UUID promotionId);
    int countByPromotionIdAndOrderId(java.util.UUID promotionId, java.util.UUID orderId);
    List<PromotionRedemption> findByOrderId(java.util.UUID orderId);

    @Modifying @Transactional
    @Query("delete from PromotionRedemption r where r.order.id = :orderId")
    int voidByOrder(java.util.UUID orderId);
}
