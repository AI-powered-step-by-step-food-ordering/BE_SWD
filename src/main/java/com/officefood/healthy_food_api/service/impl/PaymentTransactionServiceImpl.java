package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.repository.PaymentTransactionRepository;
import com.officefood.healthy_food_api.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class PaymentTransactionServiceImpl extends CrudServiceImpl<PaymentTransaction> implements PaymentTransactionService {
    private final PaymentTransactionRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<PaymentTransaction, String> repo() {
        return repository;
    }

    @Override public void authorize(String paymentId) { repository.findById(paymentId).orElseThrow(); /* TODO */ }
    @Override public void capture(String paymentId) { repository.findById(paymentId).orElseThrow(); /* TODO */ }
    @Override public void refund(String paymentId) { repository.findById(paymentId).orElseThrow(); /* TODO */ }

    @Override
    public java.util.List<PaymentTransaction> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public java.util.List<PaymentTransaction> findByOrderId(String orderId) {
        return repository.findByOrderId(orderId);
    }
}
