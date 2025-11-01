package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.BowlTemplateRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlTemplateResponse;
import com.officefood.healthy_food_api.mapper.BowlTemplateMapper;
import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/bowl_templates")
@RequiredArgsConstructor
public class BowlTemplateController extends BaseController<BowlTemplate, BowlTemplateRequest, BowlTemplateResponse> {
    private final ServiceProvider sp;
    private final BowlTemplateMapper mapper;

    @Override
    protected CrudService<BowlTemplate> getService() {
        return sp.bowlTemplates();
    }

    @Override
    protected BowlTemplateResponse toResponse(BowlTemplate entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected BowlTemplate toEntity(BowlTemplateRequest request) {
        return mapper.toEntity(request);
    }

    // POST /api/bowl_templates/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> create(@Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplateResponse response = mapper.toResponse(sp.bowlTemplates().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl template created successfully", response));
    }

    // PUT /api/bowl_templates/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> update(@PathVariable String id,
                              @Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplate entity = mapper.toEntity(req);
        entity.setId(id);
        BowlTemplateResponse response = mapper.toResponse(sp.bowlTemplates().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl template updated successfully", response));
    }
}
