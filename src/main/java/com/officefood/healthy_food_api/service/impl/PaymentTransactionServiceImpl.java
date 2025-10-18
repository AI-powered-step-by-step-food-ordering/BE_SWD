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
    protected org.springframework.data.jpa.repository.JpaRepository<PaymentTransaction, java.util.UUID> repo() {
        return repository;
    }
}