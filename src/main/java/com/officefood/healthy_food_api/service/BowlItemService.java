package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.BowlItem;

import java.util.List;
import java.util.Optional;

public interface BowlItemService extends CrudService<BowlItem> {
    void changeQuantity(String bowlItemId, int qty);
    List<BowlItem> findAllWithIngredient();
    Optional<BowlItem> findByIdWithIngredient(String id);
}
