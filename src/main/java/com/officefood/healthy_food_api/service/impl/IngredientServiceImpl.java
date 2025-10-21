package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.repository.IngredientRepository;
import com.officefood.healthy_food_api.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientServiceImpl extends CrudServiceImpl<Ingredient> implements IngredientService {
    private final IngredientRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Ingredient, UUID> repo() {
        return repository;
    }

    @Override public void markOutOfStock(UUID ingredientId) { repository.findById(ingredientId).orElseThrow(); /* TODO */ }

}
