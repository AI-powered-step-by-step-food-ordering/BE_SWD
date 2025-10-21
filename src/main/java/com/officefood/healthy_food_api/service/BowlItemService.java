package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.BowlItem;

public interface BowlItemService extends CrudService<BowlItem> {
    void changeQuantity(java.util.UUID bowlItemId, int qty);
}
