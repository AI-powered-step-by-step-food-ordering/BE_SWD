package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends UuidJpaRepository<Order> {

    @Query("""
           select coalesce(sum(bi.quantity * bi.unitPrice), 0)
           from BowlItem bi
           where bi.bowl.order.id = :orderId
           """)
    long calcSubtotal(@Param("orderId") String orderId);

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
    long calcTotalDiscount(@Param("orderId") String orderId);

    // Get orders by user ID ordered by creation date descending
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserId(@Param("userId") String userId);

    // Get all orders with bowls and user joined (without template to avoid Cartesian product)
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.store s " +
           "LEFT JOIN FETCH o.user u " +
           "LEFT JOIN FETCH o.bowls b " +
           "WHERE o.isActive = true " +
           "AND (b.isActive = true OR b IS NULL) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findAllWithBowlsAndUser();

    // Helper query to fetch templates for bowls
    @Query("SELECT DISTINCT b FROM Bowl b " +
           "LEFT JOIN FETCH b.template t " +
           "WHERE b.id IN :bowlIds " +
           "AND b.isActive = true " +
           "AND (t.isActive = true OR t IS NULL)")
    List<com.officefood.healthy_food_api.model.Bowl> fetchBowlTemplates(@Param("bowlIds") List<String> bowlIds);

    // Get order by ID with bowls and user joined
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.store s " +
           "LEFT JOIN FETCH o.user u " +
           "LEFT JOIN FETCH o.bowls b " +
           "LEFT JOIN FETCH b.template t " +
           "WHERE o.id = :id " +
           "AND o.isActive = true " +
           "AND (b.isActive = true OR b IS NULL) " +
           "AND (t.isActive = true OR t IS NULL)")
    Optional<Order> findByIdWithBowlsAndUser(@Param("id") String id);

    // Get orders by user ID with bowls and user joined
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.store s " +
           "LEFT JOIN FETCH o.user u " +
           "LEFT JOIN FETCH o.bowls b " +
           "WHERE o.user.id = :userId " +
           "AND o.isActive = true " +
           "AND (b.isActive = true OR b IS NULL) " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithBowlsAndUser(@Param("userId") String userId);
}
