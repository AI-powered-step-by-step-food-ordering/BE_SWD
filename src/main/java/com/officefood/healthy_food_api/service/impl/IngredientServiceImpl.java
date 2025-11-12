package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.request.IngredientSearchRequest;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.repository.IngredientRepository;
import com.officefood.healthy_food_api.service.IngredientService;
import com.officefood.healthy_food_api.specification.IngredientSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class IngredientServiceImpl extends CrudServiceImpl<Ingredient> implements IngredientService {
    private final IngredientRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Ingredient, String> repo() {
        return repository;
    }

    @Override public void markOutOfStock(String ingredientId) { repository.findById(ingredientId).orElseThrow(); /* TODO */ }

    @Override
    public List<Ingredient> findByCategoryId(String categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> search(IngredientSearchRequest searchRequest) {
        // Build specification from search request
        Specification<Ingredient> spec = IngredientSpecifications.withSearchCriteria(searchRequest);

        // Execute search
        List<Ingredient> ingredients = repository.findAll(spec);

        return ingredients;
    }

}
