package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.PromotionRedemptionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PromotionRedemptionResponse;
import com.officefood.healthy_food_api.mapper.PromotionRedemptionMapper;
import com.officefood.healthy_food_api.model.PromotionRedemption;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotion_redemptions")
@RequiredArgsConstructor
public class PromotionRedemptionController {
    private final ServiceProvider sp;
    private final PromotionRedemptionMapper mapper;

    // GET /api/promotion_redemptions/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<PromotionRedemptionResponse>>> getAll() {
        List<PromotionRedemptionResponse> promotionRedemptions = sp.promotionRedemptions()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion redemptions retrieved successfully", promotionRedemptions));
    }

    // GET /api/promotion_redemptions/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<PromotionRedemptionResponse>> getById(@PathVariable UUID id) {
        return sp.promotionRedemptions()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(promotionRedemption -> ResponseEntity.ok(ApiResponse.success(200, "Promotion redemption retrieved successfully", promotionRedemption)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Promotion redemption not found")));
    }

    // POST /api/promotion_redemptions/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PromotionRedemptionResponse>> create(@Valid @RequestBody PromotionRedemptionRequest req) {
        PromotionRedemptionResponse response = mapper.toResponse(sp.promotionRedemptions().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Promotion redemption created successfully", response));
    }

    // PUT /api/promotion_redemptions/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PromotionRedemptionResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody PromotionRedemptionRequest req) {
        PromotionRedemption entity = mapper.toEntity(req);
        entity.setId(id);
        PromotionRedemptionResponse response = mapper.toResponse(sp.promotionRedemptions().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion redemption updated successfully", response));
    }

    // DELETE /api/promotion_redemptions/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.promotionRedemptions().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion redemption deleted successfully", null));
    }
}
