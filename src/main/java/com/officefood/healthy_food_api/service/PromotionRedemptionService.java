package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.PromotionRedemption;

public interface PromotionRedemptionService extends CrudService<PromotionRedemption> {
    void voidRedemption(String redemptionId);
}
