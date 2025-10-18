package com.officefood.healthy_food_api.service;
import com.officefood.healthy_food_api.model.Inventory;
public interface InventoryService extends CrudService<Inventory> {
    double getBalance(java.util.UUID storeId, java.util.UUID ingredientId);
    void reserveForOrder(java.util.UUID orderId);
    void consumeForOrder(java.util.UUID orderId);
    void releaseReservationForOrder(java.util.UUID orderId);}
