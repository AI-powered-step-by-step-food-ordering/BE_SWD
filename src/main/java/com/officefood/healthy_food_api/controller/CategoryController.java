package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.CategoryRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.mapper.CategoryMapper;
import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController extends BaseController<Category, CategoryRequest, CategoryResponse> {
    private final ServiceProvider sp;
    private final CategoryMapper mapper;

    @Override
    protected CrudService<Category> getService() {
        return sp.categories();
    }

    @Override
    protected CategoryResponse toResponse(Category entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected Category toEntity(CategoryRequest request) {
        return mapper.toEntity(request);
    }

    // POST /api/categories/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest req) {
        CategoryResponse response = mapper.toResponse(sp.categories().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Category created successfully", response));
    }

    // PUT /api/categories/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody CategoryRequest req) {
        Category entity = mapper.toEntity(req);
        entity.setId(id);
        CategoryResponse response = mapper.toResponse(sp.categories().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Category updated successfully", response));
    }
}
