package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Store;

public interface StoreService extends CrudService<Store> {
    void open(java.util.UUID storeId); void close(java.util.UUID storeId);
}
