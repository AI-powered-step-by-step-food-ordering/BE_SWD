package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.IngredientRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.IngredientResponse;
import com.officefood.healthy_food_api.mapper.IngredientMapper;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {
    private final ServiceProvider sp;
    private final IngredientMapper mapper;

    // GET /api/ingredients/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<IngredientResponse>>> getAll() {
        List<IngredientResponse> ingredients = sp.ingredients()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredients retrieved successfully", ingredients));
    }

    // GET /api/ingredients/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<IngredientResponse>> getById(@PathVariable UUID id) {
        return sp.ingredients()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(ingredient -> ResponseEntity.ok(ApiResponse.success(200, "Ingredient retrieved successfully", ingredient)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Ingredient not found")));
    }

    // POST /api/ingredients/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<IngredientResponse>> create(@Valid @RequestBody IngredientRequest req) {
        IngredientResponse response = mapper.toResponse(sp.ingredients().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Ingredient created successfully", response));
    }

    // PUT /api/ingredients/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<IngredientResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody IngredientRequest req) {
        Ingredient entity = mapper.toEntity(req);
        entity.setId(id);
        IngredientResponse response = mapper.toResponse(sp.ingredients().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient updated successfully", response));
    }

    // DELETE /api/ingredients/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.ingredients().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient deleted successfully", null));
    }
}
