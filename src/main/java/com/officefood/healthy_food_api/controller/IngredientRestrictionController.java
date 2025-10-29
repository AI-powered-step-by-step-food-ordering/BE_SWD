package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.IngredientRestrictionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.IngredientRestrictionResponse;
import com.officefood.healthy_food_api.dto.response.IngredientValidationResult;
import com.officefood.healthy_food_api.mapper.IngredientRestrictionMapper;
import com.officefood.healthy_food_api.model.IngredientRestriction;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ingredient-restrictions")
@RequiredArgsConstructor
public class IngredientRestrictionController {
    private final ServiceProvider sp;
    private final IngredientRestrictionMapper mapper;

    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<IngredientRestrictionResponse>>> getAll() {
        List<IngredientRestrictionResponse> restrictions = sp.ingredientRestrictions()
                .findAll()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient restrictions retrieved successfully", restrictions));
    }

    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<IngredientRestrictionResponse>> getById(@PathVariable UUID id) {
        return sp.ingredientRestrictions()
                .findById(id)
                .map(mapper::toResponse)
                .map(restriction -> ResponseEntity.ok(ApiResponse.success(200, "Ingredient restriction retrieved successfully", restriction)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Ingredient restriction not found")));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<IngredientRestrictionResponse>> create(@Valid @RequestBody IngredientRestrictionRequest req) {
        IngredientRestrictionResponse response = mapper.toResponse(sp.ingredientRestrictions().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Ingredient restriction created successfully", response));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<IngredientRestrictionResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody IngredientRestrictionRequest req) {
        IngredientRestriction entity = mapper.toEntity(req);
        entity.setId(id);
        IngredientRestrictionResponse response = mapper.toResponse(sp.ingredientRestrictions().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient restriction updated successfully", response));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.ingredientRestrictions().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient restriction deleted successfully", null));
    }

    // API để validate ingredient trước khi thêm vào bowl
    @PostMapping("/validate-addition")
    public ResponseEntity<ApiResponse<IngredientValidationResult>> validateIngredientAddition(
            @RequestParam UUID bowlId,
            @RequestParam UUID ingredientId) {
        IngredientValidationResult result = sp.ingredientRestrictions().validateIngredientAddition(bowlId, ingredientId);
        return ResponseEntity.ok(ApiResponse.success(200, "Validation completed", result));
    }

    // API để lấy danh sách ingredients bị restrict trong bowl
    @GetMapping("/restricted-ingredients/{bowlId}")
    public ResponseEntity<ApiResponse<List<UUID>>> getRestrictedIngredients(@PathVariable UUID bowlId) {
        List<UUID> restrictedIds = sp.ingredientRestrictions().getRestrictedIngredientsForBowl(bowlId);
        return ResponseEntity.ok(ApiResponse.success(200, "Restricted ingredients retrieved successfully", restrictedIds));
    }
}
