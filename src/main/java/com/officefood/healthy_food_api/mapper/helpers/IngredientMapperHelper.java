package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.Ingredient;
public final class IngredientMapperHelper {
    private IngredientMapperHelper() { }
    public static Ingredient ingredient(java.util.UUID id) {
        if (id == null) return null;
        Ingredient x = new Ingredient();
        x.setId(id);
        return x;
    }
}
