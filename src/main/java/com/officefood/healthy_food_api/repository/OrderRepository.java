package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends UuidJpaRepository<Order>, JpaSpecificationExecutor<Order> {

    @Query("""
           select coalesce(sum(bi.quantity * bi.unitPrice), 0)
           from BowlItem bi
           where bi.bowl.order.id = :orderId
           """)
    long calcSubtotal(@Param("orderId") String orderId);

    @Query(value = """
           SELECT COALESCE(SUM(o.subtotal_amount * p.discount_percent / 100.0), 0.0)
           FROM promotion_redemptions pr
           JOIN orders o ON CAST(pr.order_id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci = CAST(o.id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci
           JOIN promotions p ON CAST(pr.promotion_id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci = CAST(p.id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci
           WHERE CAST(o.id AS CHAR CHARACTER SET utf8mb4) COLLATE utf8mb4_unicode_ci = :orderId 
           AND pr.status = 'APPLIED'
           AND pr.is_active = 1
           """, nativeQuery = true)
    double calcTotalDiscount(@Param("orderId") String orderId);

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

    // Helper query to fetch templates WITH STEPS for bowls
    @Query("SELECT DISTINCT b FROM Bowl b " +
           "LEFT JOIN FETCH b.template t " +
           "LEFT JOIN FETCH t.steps ts " +
           "LEFT JOIN FETCH ts.category c " +
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

    // Get order by ID with bowls for recalculation (step 1)
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.bowls b " +
           "WHERE o.id = :id " +
           "AND o.isActive = true")
    Optional<Order> findByIdWithBowls(@Param("id") String id);

    // Get bowls with items and ingredients for recalculation (step 2)
    @Query("SELECT DISTINCT b FROM Bowl b " +
           "LEFT JOIN FETCH b.items bi " +
           "LEFT JOIN FETCH bi.ingredient i " +
           "WHERE b.order.id = :orderId " +
           "AND b.isActive = true")
    List<com.officefood.healthy_food_api.model.Bowl> findBowlsWithItemsByOrderId(@Param("orderId") String orderId);
}
