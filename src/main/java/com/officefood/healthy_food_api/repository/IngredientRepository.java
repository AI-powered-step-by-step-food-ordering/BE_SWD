package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;

import java.util.List;

public interface IngredientRepository extends UuidJpaRepository<Ingredient> {
    List<Ingredient> findByCategoryId(String categoryId);
}
