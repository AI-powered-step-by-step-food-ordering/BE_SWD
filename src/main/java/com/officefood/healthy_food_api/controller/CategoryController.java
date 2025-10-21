package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.CategoryRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.mapper.CategoryMapper;
import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ServiceProvider sp;
    private final CategoryMapper mapper;

    // GET /api/categories/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        List<CategoryResponse> categories = sp.categories()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Categories retrieved successfully", categories));
    }

    // GET /api/categories/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        return sp.categories()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(category -> ResponseEntity.ok(ApiResponse.success(200, "Category retrieved successfully", category)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Category not found")));
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

    // DELETE /api/categories/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.categories().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Category deleted successfully", null));
    }
}
