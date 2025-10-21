package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Inventory;

public interface InventoryService extends CrudService<Inventory> {
    void reserveForOrder(java.util.UUID orderId); void consumeForOrder(java.util.UUID orderId); void returnForCancelledOrder(java.util.UUID orderId);
}
