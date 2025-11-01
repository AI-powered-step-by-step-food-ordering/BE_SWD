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
    public ResponseEntity<ApiResponse<IngredientRestrictionResponse>> getById(@PathVariable String id) {
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
    public ResponseEntity<ApiResponse<IngredientRestrictionResponse>> update(@PathVariable String id,
                              @Valid @RequestBody IngredientRestrictionRequest req) {
        IngredientRestriction entity = mapper.toEntity(req);
        entity.setId(id);
        IngredientRestrictionResponse response = mapper.toResponse(sp.ingredientRestrictions().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient restriction updated successfully", response));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.ingredientRestrictions().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient restriction deleted successfully", null));
    }

    // API Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ validate ingredient trÃƒâ€ Ã‚Â°ÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºc khi thÃƒÆ’Ã‚Âªm vÃƒÆ’Ã‚Â o bowl
    @PostMapping("/validate-addition")
    public ResponseEntity<ApiResponse<IngredientValidationResult>> validateIngredientAddition(
            @RequestParam String bowlId,
            @RequestParam String ingredientId) {
        IngredientValidationResult result = sp.ingredientRestrictions().validateIngredientAddition(bowlId, ingredientId);
        return ResponseEntity.ok(ApiResponse.success(200, "Validation completed", result));
    }

    // API Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã†â€™ lÃƒÂ¡Ã‚ÂºÃ‚Â¥y danh sÃƒÆ’Ã‚Â¡ch ingredients bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ restrict trong bowl
    @GetMapping("/restricted-ingredients/{bowlId}")
    public ResponseEntity<ApiResponse<List<String>>> getRestrictedIngredients(@PathVariable String bowlId) {
        List<String> restrictedIds = sp.ingredientRestrictions().getRestrictedIngredientsForBowl(bowlId);
        return ResponseEntity.ok(ApiResponse.success(200, "Restricted ingredients retrieved successfully", restrictedIds));
    }
}
