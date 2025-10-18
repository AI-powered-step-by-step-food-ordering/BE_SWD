package com.officefood.healthy_food_api.repository;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository
public class PromotionRedemptionRepositoryImpl implements PromotionRedemptionRepositoryCustom {
    @PersistenceContext private EntityManager em;
    @Override public long countAllCustom() {
        return em.createQuery("select count(e) from PromotionRedemption e", Long.class).getSingleResult();
    }
}
