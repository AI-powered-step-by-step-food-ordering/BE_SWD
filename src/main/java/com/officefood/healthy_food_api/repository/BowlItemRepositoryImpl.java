package com.officefood.healthy_food_api.repository;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository
public class BowlItemRepositoryImpl implements BowlItemRepositoryCustom {
    @PersistenceContext private EntityManager em;
    @Override public long countAllCustom() {
        return em.createQuery("select count(e) from BowlItem e", Long.class).getSingleResult();
    }
}
