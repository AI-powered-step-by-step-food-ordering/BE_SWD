package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import java.util.List;

public interface PaymentTransactionService extends CrudService<PaymentTransaction> {
    void authorize(String paymentId); void capture(String paymentId); void refund(String paymentId);
    List<PaymentTransaction> findByUserId(String userId);
}
