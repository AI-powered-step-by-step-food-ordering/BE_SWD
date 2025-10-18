package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.model.enums.PaymentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, UUID>, PaymentTransactionRepositoryCustom {

    List<PaymentTransaction> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from PaymentTransaction p
        where p.order.id = :orderId
          and p.status = :status
    """)
    BigDecimal sumAmountByOrderAndStatus(@Param("orderId") UUID orderId,
                                         @Param("status") PaymentStatus status);

    // Giữ method "sumCaptured" nhưng implement bằng default method để khỏi viết enum literal trong JPQL
    default BigDecimal sumCaptured(UUID orderId) {
        return sumAmountByOrderAndStatus(orderId, PaymentStatus.CAPTURED);
    }
}
