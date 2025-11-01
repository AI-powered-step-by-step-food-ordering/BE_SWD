package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Inventory;

public interface InventoryService extends CrudService<Inventory> {
    void reserveForOrder(String orderId); void consumeForOrder(String orderId); void returnForCancelledOrder(String orderId);
}
