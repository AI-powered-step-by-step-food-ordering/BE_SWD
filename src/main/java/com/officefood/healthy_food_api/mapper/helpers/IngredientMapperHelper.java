package com.officefood.healthy_food_api.mapper.helpers;

import com.officefood.healthy_food_api.model.Ingredient;

import java.util.UUID;

public class IngredientMapperHelper {
    public static Ingredient ingredient(UUID id) {
        if (id == null) return null;
        Ingredient ingredient = new Ingredient();
        ingredient.setId(id);
        return ingredient;
    }
}
