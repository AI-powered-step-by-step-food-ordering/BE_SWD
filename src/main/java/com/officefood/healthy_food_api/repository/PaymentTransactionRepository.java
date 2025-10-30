package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;

import java.util.Optional;

public interface PaymentTransactionRepository extends UuidJpaRepository<PaymentTransaction> {
    Optional<PaymentTransaction> findByProviderTxnId(String providerTxnId);
}
