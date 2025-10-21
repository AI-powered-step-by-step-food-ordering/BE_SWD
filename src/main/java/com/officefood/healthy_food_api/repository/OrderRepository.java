package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface OrderRepository extends UuidJpaRepository<Order> {

    @Query("""
           select coalesce(sum(bi.quantity * bi.unitPrice), 0)
           from BowlItem bi
           where bi.bowl.order.id = :orderId
           """)
    long calcSubtotal(@Param("orderId") UUID orderId);

    @Query("""
           select coalesce(sum(
               case 
                   when pr.promotion.type = com.officefood.healthy_food_api.model.enums.PromotionType.PERCENT_OFF 
                   then (pr.order.subtotalAmount * pr.promotion.percentOff / 100)
                   when pr.promotion.type = com.officefood.healthy_food_api.model.enums.PromotionType.AMOUNT_OFF 
                   then pr.promotion.amountOff
                   else 0
               end
           ), 0)
           from PromotionRedemption pr
           where pr.order.id = :orderId and pr.status = com.officefood.healthy_food_api.model.enums.RedemptionStatus.APPLIED
           """)
    long calcTotalDiscount(@Param("orderId") UUID orderId);
}
