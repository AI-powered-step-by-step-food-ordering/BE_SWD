package com.officefood.healthy_food_api.repository;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepositoryImpl implements TokenRepositoryCustom {
    @PersistenceContext private EntityManager em;
    @Override public long countAllCustom() {
        return em.createQuery("select count(e) from Token e", Long.class).getSingleResult();
    }
}
