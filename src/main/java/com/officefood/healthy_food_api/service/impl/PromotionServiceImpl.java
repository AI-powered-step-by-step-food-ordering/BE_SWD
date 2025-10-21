package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.repository.PromotionRepository;
import com.officefood.healthy_food_api.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionServiceImpl extends CrudServiceImpl<Promotion> implements PromotionService {
    private final PromotionRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Promotion, UUID> repo() {
        return repository;
    }

    @Override public void activatePromotion(String code) { /* TODO */ }
    @Override public void deactivatePromotion(String code) { /* TODO */ }

}
