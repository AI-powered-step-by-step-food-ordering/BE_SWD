package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlTemplateRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlTemplateResponse;
import com.officefood.healthy_food_api.mapper.BowlTemplateMapper;
import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bowl_templates")
@RequiredArgsConstructor
public class BowlTemplateController {
    private final ServiceProvider sp;
    private final BowlTemplateMapper mapper;

    // GET /api/bowl_templates/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<BowlTemplateResponse>>> getAll() {
        List<BowlTemplateResponse> bowlTemplates = sp.bowlTemplates()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl templates retrieved successfully", bowlTemplates));
    }

    // GET /api/bowl_templates/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> getById(@PathVariable UUID id) {
        return sp.bowlTemplates()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(bowlTemplate -> ResponseEntity.ok(ApiResponse.success(200, "Bowl template retrieved successfully", bowlTemplate)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl template not found")));
    }

    // POST /api/bowl_templates/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> create(@Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplateResponse response = mapper.toResponse(sp.bowlTemplates().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl template created successfully", response));
    }

    // PUT /api/bowl_templates/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplate entity = mapper.toEntity(req);
        entity.setId(id);
        BowlTemplateResponse response = mapper.toResponse(sp.bowlTemplates().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl template updated successfully", response));
    }

    // DELETE /api/bowl_templates/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.bowlTemplates().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl template deleted successfully", null));
    }
}
