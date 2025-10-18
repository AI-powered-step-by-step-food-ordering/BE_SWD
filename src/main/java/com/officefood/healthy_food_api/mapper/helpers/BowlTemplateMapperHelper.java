package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.BowlTemplate;
public final class BowlTemplateMapperHelper {
    private BowlTemplateMapperHelper() { }
    public static BowlTemplate bowlTemplate(java.util.UUID id) {
        if (id == null) return null;
        BowlTemplate x = new BowlTemplate();
        x.setId(id);
        return x;
    }
}
