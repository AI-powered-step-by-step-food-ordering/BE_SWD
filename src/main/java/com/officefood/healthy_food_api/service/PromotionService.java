package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Promotion;

public interface PromotionService extends CrudService<Promotion> {
    void activatePromotion(java.lang.String code); void deactivatePromotion(java.lang.String code);
}
