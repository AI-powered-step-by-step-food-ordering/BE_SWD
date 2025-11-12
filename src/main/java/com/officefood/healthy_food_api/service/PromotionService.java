package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.request.PromotionSearchRequest;
import com.officefood.healthy_food_api.model.Promotion;

import java.util.List;

public interface PromotionService extends CrudService<Promotion> {
    void activatePromotion(java.lang.String code); void deactivatePromotion(java.lang.String code);

    // Search functionality
    List<Promotion> search(PromotionSearchRequest searchRequest);
}
