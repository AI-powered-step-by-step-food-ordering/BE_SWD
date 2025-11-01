package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.Bowl;
public final class BowlMapperHelper {
    private BowlMapperHelper() { }
    public static Bowl bowl(String id) {
        if (id == null) return null;
        Bowl x = new Bowl();
        x.setId(id);
        return x;
    }
}
