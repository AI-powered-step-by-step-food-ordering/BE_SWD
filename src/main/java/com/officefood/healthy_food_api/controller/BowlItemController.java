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
                 .findAllWithIngredient()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl items retrieved successfully", bowlItems));
    }

    // GET /api/bowl_items/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlItemResponse>> getById(@PathVariable String id) {
        return sp.bowlItems()
                 .findByIdWithIngredient(id)
                 .map(mapper::toResponse)
                 .map(bowlItem -> ResponseEntity.ok(ApiResponse.success(200, "Bowl item retrieved successfully", bowlItem)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl item not found")));
    }

    // POST /api/bowl_items/create - vÃ¡Â»â€ºi validation rÃƒÂ ng buÃ¡Â»â„¢c ingredients
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlItemResponse>> create(@Valid @RequestBody BowlItemRequest req) {
        // Validate ingredient restrictions trÃ†Â°Ã¡Â»â€ºc khi tÃ¡ÂºÂ¡o
        IngredientValidationResult validationResult = sp.ingredientRestrictions()
                .validateIngredientAddition(req.getBowlId(), req.getIngredientId());

        if (!validationResult.isValid()) {
            return ResponseEntity.ok(ApiResponse.error(400, "INGREDIENT_RESTRICTION_VIOLATED", validationResult.getMessage()));
        }

        BowlItemResponse response = mapper.toResponse(sp.bowlItems().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl item created successfully", response));
    }

    // POST /api/bowl_items/create-bulk - Tạo nhiều bowl items cùng lúc
    @PostMapping("/create-bulk")
    public ResponseEntity<ApiResponse<List<BowlItemResponse>>> createBulk(@Valid @RequestBody List<BowlItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(400, "EMPTY_LIST", "Request list cannot be empty"));
        }

        List<BowlItemResponse> responses = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        for (int i = 0; i < requests.size(); i++) {
            BowlItemRequest req = requests.get(i);
            try {
                // Validate ingredient restrictions cho từng item
                IngredientValidationResult validationResult = sp.ingredientRestrictions()
                        .validateIngredientAddition(req.getBowlId(), req.getIngredientId());

                if (!validationResult.isValid()) {
                    errors.add("Item " + (i + 1) + ": " + validationResult.getMessage());
                    continue;
                }

                // Tạo bowl item
                BowlItem createdItem = sp.bowlItems().create(mapper.toEntity(req));
                responses.add(mapper.toResponse(createdItem));

            } catch (Exception e) {
                errors.add("Item " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Nếu có lỗi, trả về thông tin chi tiết
        if (!errors.isEmpty()) {
            String errorMessage = "Some items failed to create: " + String.join("; ", errors);
            if (responses.isEmpty()) {
                // Tất cả đều thất bại
                return ResponseEntity.ok(ApiResponse.error(400, "ALL_ITEMS_FAILED", errorMessage));
            } else {
                // Một số thành công, một số thất bại
                return ResponseEntity.ok(ApiResponse.success(207, errorMessage, responses));
            }
        }

        // Tất cả thành công
        return ResponseEntity.ok(ApiResponse.success(201, "All bowl items created successfully", responses));
    }

    // PUT /api/bowl_items/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlItemResponse>> update(@PathVariable String id,
                              @Valid @RequestBody BowlItemRequest req) {
        BowlItem entity = mapper.toEntity(req);
        entity.setId(id);
        BowlItemResponse response = mapper.toResponse(sp.bowlItems().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl item updated successfully", response));
    }

    // DELETE /api/bowl_items/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.bowlItems().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl item deleted successfully", null));
    }
}
