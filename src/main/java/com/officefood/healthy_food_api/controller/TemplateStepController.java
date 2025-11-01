package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.TemplateStepRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.TemplateStepResponse;
import com.officefood.healthy_food_api.mapper.TemplateStepMapper;
import com.officefood.healthy_food_api.model.TemplateStep;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/template_steps")
@RequiredArgsConstructor
public class TemplateStepController {
    private final ServiceProvider sp;
    private final TemplateStepMapper mapper;

    // GET /api/template_steps/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<TemplateStepResponse>>> getAll() {
        List<TemplateStepResponse> templateSteps = sp.templateSteps()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Template steps retrieved successfully", templateSteps));
    }

    // GET /api/template_steps/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<TemplateStepResponse>> getById(@PathVariable String id) {
        return sp.templateSteps()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(templateStep -> ResponseEntity.ok(ApiResponse.success(200, "Template step retrieved successfully", templateStep)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Template step not found")));
    }

    // POST /api/template_steps/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TemplateStepResponse>> create(@Valid @RequestBody TemplateStepRequest req) {
        TemplateStepResponse response = mapper.toResponse(sp.templateSteps().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Template step created successfully", response));
    }

    // PUT /api/template_steps/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<TemplateStepResponse>> update(@PathVariable String id,
                              @Valid @RequestBody TemplateStepRequest req) {
        TemplateStep entity = mapper.toEntity(req);
        entity.setId(id);
        TemplateStepResponse response = mapper.toResponse(sp.templateSteps().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Template step updated successfully", response));
    }

    // DELETE /api/template_steps/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.templateSteps().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Template step deleted successfully", null));
    }
}
