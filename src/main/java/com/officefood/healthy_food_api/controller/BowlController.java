package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlRequest;
import com.officefood.healthy_food_api.dto.request.BowlSearchRequest;
import com.officefood.healthy_food_api.dto.request.CreateBowlFromTemplateRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.BowlMapper;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.TemplateStepEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bowls")
@RequiredArgsConstructor
public class BowlController {
    private final ServiceProvider sp;
    private final BowlMapper mapper;
    private final TemplateStepEnrichmentService enrichmentService;

    // GET /api/bowls/search - Search endpoint
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BowlResponse>>> search(
            @ModelAttribute BowlSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Execute search
        List<Bowl> bowls = sp.bowls().search(searchRequest);

        // Apply sorting
        List<Bowl> sortedBowls = sortBowls(bowls, sortBy, sortDir);

        // Create paged response
        PagedResponse<BowlResponse> pagedResponse = createPagedResponse(sortedBowls, page, size);

        return ResponseEntity.ok(ApiResponse.success(200,
            "Bowls search completed successfully. Found " + bowls.size() + " results.",
            pagedResponse));
    }

    // GET /api/bowls/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<BowlResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<Bowl> allBowls = sp.bowls().findAllWithTemplateAndSteps();
        List<Bowl> sortedBowls = sortBowls(allBowls, sortBy, sortDir);
        PagedResponse<BowlResponse> pagedResponse = createPagedResponse(sortedBowls, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowls retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to sort bowls
     */
    private List<Bowl> sortBowls(List<Bowl> bowls, String sortBy, String sortDir) {
        if (bowls == null || bowls.isEmpty()) {
            return bowls;
        }

        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        try {
            Comparator<Bowl> comparator = (bowl1, bowl2) -> {
                try {
                    Object value1 = getFieldValue(bowl1, sortBy);
                    Object value2 = getFieldValue(bowl2, sortBy);

                    if (value1 == null && value2 == null) return 0;
                    if (value1 == null) return 1;
                    if (value2 == null) return -1;

                    int result = compareValues(value1, value2);
                    return ascending ? result : -result;
                } catch (Exception e) {
                    return 0;
                }
            };

            return bowls.stream().sorted(comparator).collect(Collectors.toList());
        } catch (Exception e) {
            return bowls;
        }
    }

    private Object getFieldValue(Bowl entity, String fieldName) throws Exception {
        Field field = findField(entity.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(entity);
        }
        return null;
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareValues(Object value1, Object value2) {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            if (value1.getClass().equals(value2.getClass())) {
                return ((Comparable) value1).compareTo(value2);
            }
        }
        return value1.toString().compareTo(value2.toString());
    }

    /**
     * Helper method to create PagedResponse from Bowl list
     */
    private PagedResponse<BowlResponse> createPagedResponse(List<Bowl> bowls, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = bowls.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<BowlResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            List<Bowl> pageBowls = bowls.stream()
                    .skip(startIndex)
                    .limit(size)
                    .collect(Collectors.toList());

            pageContent = pageBowls.stream()
                    .map(entity -> {
                        BowlResponse response = mapper.toResponse(entity);
                        enrichBowlResponse(response, entity);
                        return response;
                    })
                    .collect(Collectors.toList());
        }

        return PagedResponse.<BowlResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    /**
     * Helper method to enrich BowlResponse with ingredient details in defaultIngredients
     */
    private void enrichBowlResponse(BowlResponse response, Bowl entity) {
        if (response.getTemplate() == null || entity.getTemplate() == null) {
            return;
        }

        if (response.getTemplate().getSteps() != null &&
            entity.getTemplate().getSteps() != null) {

            // Enrich each template step's defaultIngredients
            entity.getTemplate().getSteps().forEach(step -> {
                response.getTemplate().getSteps().stream()
                    .filter(stepRes -> stepRes.getId().equals(step.getId()))
                    .findFirst()
                    .ifPresent(stepRes -> enrichmentService.enrichDefaultIngredients(stepRes, step));
            });
        }
    }

    // GET /api/bowls/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlResponse>> getById(@PathVariable String id) {
        return sp.bowls()
                 .findByIdWithTemplateAndItems(id)
                 .map(entity -> {
                     BowlResponse response = mapper.toResponse(entity);
                     enrichBowlResponse(response, entity);
                     return response;
                 })
                 .map(bowl -> ResponseEntity.ok(ApiResponse.success(200, "Bowl retrieved successfully", bowl)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl not found")));
    }

    // GET /api/bowls/getbyid/{id}/with-items - Get bowl with all items
    @GetMapping("/getbyid/{id}/with-items")
    public ResponseEntity<ApiResponse<BowlResponse>> getByIdWithItems(@PathVariable String id) {
        return sp.bowls()
                 .findByIdWithTemplateAndItems(id)
                 .map(mapper::toResponse)
                 .map(bowl -> ResponseEntity.ok(ApiResponse.success(200, "Bowl with items retrieved successfully", bowl)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl not found")));
    }

    // POST /api/bowls/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlResponse>> create(@Valid @RequestBody BowlRequest req) {
        BowlResponse response = mapper.toResponse(sp.bowls().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl created successfully", response));
    }

    // PUT /api/bowls/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlResponse>> update(@PathVariable String id,
                              @Valid @RequestBody BowlRequest req) {
        Bowl entity = mapper.toEntity(req);
        entity.setId(id);
        BowlResponse response = mapper.toResponse(sp.bowls().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl updated successfully", response));
    }

    // DELETE /api/bowls/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.bowls().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl deleted successfully", null));
    }

    /**
     * POST /api/bowls/create-from-template
     * Tạo Bowl từ template với default ingredients tự động
     * Sử dụng default quantities từ template (isDefault=true)
     */
    @PostMapping("/create-from-template")
    public ResponseEntity<ApiResponse<BowlResponse>> createFromTemplate(
            @Valid @RequestBody CreateBowlFromTemplateRequest req) {
        try {
            // Tạo bowl từ template với default quantities
            Bowl bowl = sp.bowls().createFromTemplate(
                req.getOrderId(),
                req.getTemplateId()
            );

            // Override custom name và instruction nếu có
            if (req.getCustomName() != null && !req.getCustomName().trim().isEmpty()) {
                bowl.setName(req.getCustomName());
            }
            if (req.getInstruction() != null && !req.getInstruction().trim().isEmpty()) {
                bowl.setInstruction(req.getInstruction());
            }

            // Update nếu có thay đổi
            if (req.getCustomName() != null || req.getInstruction() != null) {
                bowl = sp.bowls().update(bowl.getId(), bowl);
            }

            // Reload bowl with template and items to prevent LazyInitializationException
            Bowl enriched = sp.bowls().findByIdWithTemplateAndItems(bowl.getId())
                .orElse(bowl); // Fallback to original if reload fails

            BowlResponse response = mapper.toResponse(enriched);
            return ResponseEntity.ok(ApiResponse.success(201,
                "Bowl created from template successfully with " + enriched.getItems().size() + " items",
                response));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(400, "CREATE_FAILED",
                "Failed to create bowl from template: " + e.getMessage()));
        }
    }
}
