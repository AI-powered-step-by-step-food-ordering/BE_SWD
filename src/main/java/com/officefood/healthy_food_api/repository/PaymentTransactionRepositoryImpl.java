package com.officefood.healthy_food_api.repository;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentTransactionRepositoryImpl implements PaymentTransactionRepositoryCustom {
    @PersistenceContext private EntityManager em;
    @Override public long countAllCustom() {
        return em.createQuery("select count(e) from PaymentTransaction e", Long.class).getSingleResult();
    }
}
