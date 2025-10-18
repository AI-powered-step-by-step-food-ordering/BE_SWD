package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.Store;
public final class StoreMapperHelper {
    private StoreMapperHelper() { }
    public static Store store(java.util.UUID id) {
        if (id == null) return null;
        Store x = new Store();
        x.setId(id);
        return x;
    }
}
