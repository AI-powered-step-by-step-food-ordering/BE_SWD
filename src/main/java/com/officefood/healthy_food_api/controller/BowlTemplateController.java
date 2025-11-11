package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.BowlTemplateRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlTemplateResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.BowlTemplateMapper;
import com.officefood.healthy_food_api.model.BowlTemplate;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import com.officefood.healthy_food_api.service.TemplateStepEnrichmentService;
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
    private final TemplateStepEnrichmentService enrichmentService;

    @Override
    protected CrudService<BowlTemplate> getService() {
        return sp.bowlTemplates();
    }

    @Override
    protected BowlTemplateResponse toResponse(BowlTemplate entity) {
        BowlTemplateResponse response = mapper.toResponse(entity);
        // Enrich defaultIngredients với thông tin ingredient đầy đủ
        if (response.getSteps() != null && !response.getSteps().isEmpty() &&
            entity.getSteps() != null && !entity.getSteps().isEmpty()) {
            entity.getSteps().forEach(step -> {
                response.getSteps().stream()
                    .filter(stepRes -> stepRes.getId().equals(step.getId()))
                    .findFirst()
                    .ifPresent(stepRes -> enrichmentService.enrichDefaultIngredients(stepRes, step));
            });
        }
        return response;
    }

    @Override
    protected BowlTemplate toEntity(BowlTemplateRequest request) {
        return mapper.toEntity(request);
    }

    @Override
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<BowlTemplateResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        java.util.List<BowlTemplate> allTemplates = sp.bowlTemplates().findAllWithSteps();
        java.util.List<BowlTemplate> sortedTemplates = sortEntities(allTemplates, sortBy, sortDir);
        PagedResponse<BowlTemplateResponse> pagedResponse = createPagedResponse(sortedTemplates, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl templates retrieved successfully", pagedResponse));
    }

    // Override to use custom query with steps and enrichment
    @Override
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> getById(@PathVariable String id) {
        return sp.bowlTemplates()
                 .findByIdWithSteps(id)
                 .map(this::toResponse) // Use toResponse() which includes enrichment
                 .map(template -> ResponseEntity.ok(ApiResponse.success(200, "Bowl template retrieved successfully", template)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl template not found")));
    }

    // GET /api/bowl_templates/with-defaults - Get templates with default ingredients
    @GetMapping("/with-defaults")
    public ResponseEntity<ApiResponse<PagedResponse<BowlTemplateResponse>>> getTemplatesWithDefaults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        java.util.List<BowlTemplate> templates = sp.bowlTemplates().findAllTemplatesWithCompleteDefaults();
        java.util.List<BowlTemplate> sortedTemplates = sortEntities(templates, sortBy, sortDir);
        PagedResponse<BowlTemplateResponse> pagedResponse = createPagedResponse(sortedTemplates, page, size);
        return ResponseEntity.ok(ApiResponse.success(200,
            "Bowl templates with complete default ingredients retrieved successfully",
            pagedResponse));
    }

    // POST /api/bowl_templates/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> create(@Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplate created = sp.bowlTemplates().create(mapper.toEntity(req));
        BowlTemplateResponse response = toResponse(created); // Use toResponse() for enrichment
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl template created successfully", response));
    }

    // PUT /api/bowl_templates/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlTemplateResponse>> update(@PathVariable String id,
                              @Valid @RequestBody BowlTemplateRequest req) {
        BowlTemplate entity = mapper.toEntity(req);
        entity.setId(id);
        BowlTemplate updated = sp.bowlTemplates().update(id, entity);
        BowlTemplateResponse response = toResponse(updated); // Use toResponse() for enrichment
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl template updated successfully", response));
    }
}
