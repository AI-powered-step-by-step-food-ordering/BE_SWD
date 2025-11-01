package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.exception.BusinessException;
import com.officefood.healthy_food_api.model.*;
import com.officefood.healthy_food_api.model.enums.*;
import com.officefood.healthy_food_api.repository.PaymentTransactionRepository;
import com.officefood.healthy_food_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentTransactionRepository repository;

    @Override public PaymentTransaction authorize(Order order, PaymentMethod method, double amount) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setOrder(order); tx.setMethod(method); tx.setStatus(PaymentStatus.AUTHORIZED); tx.setAmount(amount);
        return repository.save(tx);
    }
    @Override public PaymentTransaction capture(Order order, String paymentId) {
        PaymentTransaction tx = repository.findById(paymentId).orElseThrow();
        if (!tx.getOrder().getId().equals(order.getId())) throw new BusinessException("Payment does not belong to order");
        tx.setStatus(PaymentStatus.CAPTURED);
        return repository.save(tx);
    }
    @Override public PaymentTransaction refund(Order order, String paymentId, double amount) {
        PaymentTransaction tx = repository.findById(paymentId).orElseThrow();
        if (!tx.getOrder().getId().equals(order.getId())) throw new BusinessException("Payment does not belong to order");
        PaymentTransaction r = new PaymentTransaction();
        r.setOrder(order); r.setMethod(tx.getMethod()); r.setStatus(PaymentStatus.REFUNDED); r.setAmount(-abs(amount));
        return repository.save(r);
    }
    private double abs(double v) { return v < 0 ? -v : v; }
}
