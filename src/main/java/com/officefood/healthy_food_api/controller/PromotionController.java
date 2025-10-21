package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.PromotionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PromotionResponse;
import com.officefood.healthy_food_api.mapper.PromotionMapper;
import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final ServiceProvider sp;
    private final PromotionMapper mapper;

    // GET /api/promotions/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getAll() {
        List<PromotionResponse> promotions = sp.promotions()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Promotions retrieved successfully", promotions));
    }

    // GET /api/promotions/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> getById(@PathVariable UUID id) {
        return sp.promotions()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(promotion -> ResponseEntity.ok(ApiResponse.success(200, "Promotion retrieved successfully", promotion)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Promotion not found")));
    }

    // POST /api/promotions/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(@Valid @RequestBody PromotionRequest req) {
        PromotionResponse response = mapper.toResponse(sp.promotions().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Promotion created successfully", response));
    }

    // PUT /api/promotions/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody PromotionRequest req) {
        Promotion entity = mapper.toEntity(req);
        entity.setId(id);
        PromotionResponse response = mapper.toResponse(sp.promotions().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion updated successfully", response));
    }

    // DELETE /api/promotions/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.promotions().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion deleted successfully", null));
    }
}
