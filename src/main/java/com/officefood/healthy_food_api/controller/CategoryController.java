package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.CategoryRequest;
import com.officefood.healthy_food_api.dto.request.CategorySearchRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.CategoryMapper;
import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


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

    // GET /api/categories/search - Simplified search endpoint (text only)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<CategoryResponse>>> search(
            @ModelAttribute CategorySearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {


        // Execute search
        java.util.List<Category> categories = sp.categories().search(searchRequest);

        // Apply sorting
        java.util.List<Category> sortedCategories = sortEntities(categories, sortBy, sortDir);

        // Create paged response
        PagedResponse<CategoryResponse> pagedResponse = createPagedResponse(sortedCategories, page, size);

        return ResponseEntity.ok(ApiResponse.success(200,
            "Categories search completed successfully. Found " + categories.size() + " results.",
            pagedResponse));
    }

    // POST /api/categories/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest req) {
        CategoryResponse response = mapper.toResponse(sp.categories().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Category created successfully", response));
    }

    // PUT /api/categories/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable String id,
                              @Valid @RequestBody CategoryRequest req) {
        Category entity = mapper.toEntity(req);
        entity.setId(id);
        CategoryResponse response = mapper.toResponse(sp.categories().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Category updated successfully", response));
    }
}
