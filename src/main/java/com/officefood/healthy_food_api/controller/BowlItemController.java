package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlItemRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlItemResponse;
import com.officefood.healthy_food_api.dto.response.IngredientValidationResult;
import com.officefood.healthy_food_api.mapper.BowlItemMapper;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bowl_items")
@RequiredArgsConstructor
public class BowlItemController {
    private final ServiceProvider sp;
    private final BowlItemMapper mapper;

    // GET /api/bowl_items/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<BowlItemResponse>>> getAll() {
        List<BowlItemResponse> bowlItems = sp.bowlItems()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl items retrieved successfully", bowlItems));
    }

    // GET /api/bowl_items/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlItemResponse>> getById(@PathVariable UUID id) {
        return sp.bowlItems()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(bowlItem -> ResponseEntity.ok(ApiResponse.success(200, "Bowl item retrieved successfully", bowlItem)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl item not found")));
    }

    // POST /api/bowl_items/create - với validation ràng buộc ingredients
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlItemResponse>> create(@Valid @RequestBody BowlItemRequest req) {
        // Validate ingredient restrictions trước khi tạo
        IngredientValidationResult validationResult = sp.ingredientRestrictions()
                .validateIngredientAddition(req.getBowlId(), req.getIngredientId());

        if (!validationResult.isValid()) {
            return ResponseEntity.ok(ApiResponse.error(400, "INGREDIENT_RESTRICTION_VIOLATED", validationResult.getMessage()));
        }

        BowlItemResponse response = mapper.toResponse(sp.bowlItems().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl item created successfully", response));
    }

    // PUT /api/bowl_items/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlItemResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody BowlItemRequest req) {
        BowlItem entity = mapper.toEntity(req);
        entity.setId(id);
        BowlItemResponse response = mapper.toResponse(sp.bowlItems().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl item updated successfully", response));
    }

    // DELETE /api/bowl_items/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.bowlItems().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl item deleted successfully", null));
    }
}
