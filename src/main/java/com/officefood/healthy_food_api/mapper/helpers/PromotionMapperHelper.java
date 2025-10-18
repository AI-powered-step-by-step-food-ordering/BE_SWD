package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.Promotion;
public final class PromotionMapperHelper {
    private PromotionMapperHelper() { }
    public static Promotion promotion(java.util.UUID id) {
        if (id == null) return null;
        Promotion x = new Promotion();
        x.setId(id);
        return x;
    }
}
