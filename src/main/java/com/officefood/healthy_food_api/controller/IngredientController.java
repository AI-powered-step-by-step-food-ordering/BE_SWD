package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.IngredientRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.IngredientResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.IngredientMapper;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController extends BaseController<Ingredient, IngredientRequest, IngredientResponse> {
    private final ServiceProvider sp;
    private final IngredientMapper mapper;

    @Override
    protected CrudService<Ingredient> getService() {
        return sp.ingredients();
    }

    @Override
    protected IngredientResponse toResponse(Ingredient entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected Ingredient toEntity(IngredientRequest request) {
        return mapper.toEntity(request);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<IngredientResponse>> create(@Valid @RequestBody IngredientRequest req) {
        IngredientResponse response = mapper.toResponse(sp.ingredients().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Ingredient created successfully", response));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<IngredientResponse>> update(@PathVariable String id,
                              @Valid @RequestBody IngredientRequest req) {
        Ingredient entity = mapper.toEntity(req);
        entity.setId(id);
        IngredientResponse response = mapper.toResponse(sp.ingredients().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient updated successfully", response));
    }
}
