package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.request.IngredientSearchRequest;
import com.officefood.healthy_food_api.model.Ingredient;

import java.util.List;

public interface IngredientService extends CrudService<Ingredient> {
    void markOutOfStock(String ingredientId);
    List<Ingredient> findByCategoryId(String categoryId);

    // Search functionality
    List<Ingredient> search(IngredientSearchRequest searchRequest);
}
