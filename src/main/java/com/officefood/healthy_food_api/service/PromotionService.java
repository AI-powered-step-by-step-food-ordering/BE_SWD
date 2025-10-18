package com.officefood.healthy_food_api.service;
import com.officefood.healthy_food_api.model.Promotion;
public interface PromotionService extends CrudService<Promotion> {
    java.util.Optional<com.officefood.healthy_food_api.model.Promotion> findActiveByCode(String code);
    void validateAndApply(com.officefood.healthy_food_api.model.Promotion promo, com.officefood.healthy_food_api.model.Order order);
}
