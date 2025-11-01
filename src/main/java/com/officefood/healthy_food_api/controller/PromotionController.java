package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.PromotionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PromotionResponse;
import com.officefood.healthy_food_api.mapper.PromotionMapper;
import com.officefood.healthy_food_api.model.Promotion;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController extends BaseController<Promotion, PromotionRequest, PromotionResponse> {
    private final ServiceProvider sp;
    private final PromotionMapper mapper;

    @Override
    protected CrudService<Promotion> getService() {
        return sp.promotions();
    }

    @Override
    protected PromotionResponse toResponse(Promotion entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected Promotion toEntity(PromotionRequest request) {
        return mapper.toEntity(request);
    }

    // POST /api/promotions/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PromotionResponse>> create(@Valid @RequestBody PromotionRequest req) {
        PromotionResponse response = mapper.toResponse(sp.promotions().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Promotion created successfully", response));
    }

    // PUT /api/promotions/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PromotionResponse>> update(@PathVariable String id,
                              @Valid @RequestBody PromotionRequest req) {
        Promotion entity = mapper.toEntity(req);
        entity.setId(id);
        PromotionResponse response = mapper.toResponse(sp.promotions().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion updated successfully", response));
    }
}
