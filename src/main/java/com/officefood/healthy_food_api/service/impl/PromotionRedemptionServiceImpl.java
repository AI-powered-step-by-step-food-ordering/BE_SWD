package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.PromotionRedemption;
import com.officefood.healthy_food_api.repository.PromotionRedemptionRepository;
import com.officefood.healthy_food_api.service.PromotionRedemptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionRedemptionServiceImpl extends CrudServiceImpl<PromotionRedemption> implements PromotionRedemptionService {
    private final PromotionRedemptionRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<PromotionRedemption, java.util.UUID> repo() {
        return repository;
    }
}