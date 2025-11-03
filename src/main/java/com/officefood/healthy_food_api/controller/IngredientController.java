package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.IngredientRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.IngredientResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.IngredientMapper;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController extends BaseController<Ingredient, IngredientRequest, IngredientResponse> {
    private final ServiceProvider sp;
    private final IngredientMapper mapper;

    @Override
    protected CrudService<Ingredient> getService() {
        return sp.ingredients();
    }

    @Override
    protected IngredientResponse toResponse(Ingredient entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected Ingredient toEntity(IngredientRequest request) {
        return mapper.toEntity(request);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<IngredientResponse>> create(@Valid @RequestBody IngredientRequest req) {
        IngredientResponse response = mapper.toResponse(sp.ingredients().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Ingredient created successfully", response));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<IngredientResponse>> update(@PathVariable String id,
                              @Valid @RequestBody IngredientRequest req) {
        Ingredient entity = mapper.toEntity(req);
        entity.setId(id);
        IngredientResponse response = mapper.toResponse(sp.ingredients().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Ingredient updated successfully", response));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagedResponse<IngredientResponse>>> getByCategory(
            @PathVariable String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        java.util.List<Ingredient> ingredients = sp.ingredients().findByCategoryId(categoryId);

        // Sort ingredients
        java.util.Comparator<Ingredient> comparator = getComparator(sortBy, sortDir);
        if (comparator != null) {
            ingredients = ingredients.stream()
                    .sorted(comparator)
                    .collect(java.util.stream.Collectors.toList());
        }

        // Pagination
        int totalElements = ingredients.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        java.util.List<IngredientResponse> content = ingredients.subList(
                Math.min(startIndex, totalElements),
                Math.min(endIndex, totalElements)
        ).stream()
                .map(mapper::toResponse)
                .collect(java.util.stream.Collectors.toList());

        PagedResponse<IngredientResponse> pagedResponse = PagedResponse.<IngredientResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(page >= totalPages - 1)
                .build();

        return ResponseEntity.ok(ApiResponse.success(200, "Ingredients retrieved by category successfully", pagedResponse));
    }

    private java.util.Comparator<Ingredient> getComparator(String sortBy, String sortDir) {
        java.util.Comparator<Ingredient> comparator = null;

        try {
            java.lang.reflect.Field field = Ingredient.class.getDeclaredField(sortBy);
            field.setAccessible(true);

            comparator = (i1, i2) -> {
                try {
                    Object val1 = field.get(i1);
                    Object val2 = field.get(i2);

                    if (val1 == null && val2 == null) return 0;
                    if (val1 == null) return 1;
                    if (val2 == null) return -1;

                    if (val1 instanceof Comparable) {
                        return ((Comparable) val1).compareTo(val2);
                    }
                    return 0;
                } catch (Exception e) {
                    return 0;
                }
            };

            if ("desc".equalsIgnoreCase(sortDir)) {
                comparator = comparator.reversed();
            }
        } catch (NoSuchFieldException e) {
            // If field doesn't exist, return null comparator
        }

        return comparator;
    }
}
