package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentTransactionRepository extends UuidJpaRepository<PaymentTransaction> {
    Optional<PaymentTransaction> findByProviderTxnId(String providerTxnId);

    // Get payment transactions by user ID through order relationship
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.order.user.id = :userId ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByUserId(@Param("userId") String userId);

    // Get payment transactions by order ID
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.order.id = :orderId ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByOrderId(@Param("orderId") String orderId);
}
