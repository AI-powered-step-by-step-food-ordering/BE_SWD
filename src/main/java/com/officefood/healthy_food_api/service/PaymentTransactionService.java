package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import java.util.List;
import java.util.UUID;

public interface PaymentTransactionService extends CrudService<PaymentTransaction> {
    void authorize(java.util.UUID paymentId); void capture(java.util.UUID paymentId); void refund(java.util.UUID paymentId);
    List<PaymentTransaction> findByUserId(UUID userId);
}
