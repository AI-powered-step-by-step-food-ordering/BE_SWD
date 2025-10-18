package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.CategoryRequest;
import com.officefood.healthy_food_api.dto.response.CategoryResponse;
import com.officefood.healthy_food_api.mapper.CategoryMapper;
import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/categorys") @RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    private final CategoryMapper mapper;

    @GetMapping("/getall") public List<CategoryResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<CategoryResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public CategoryResponse create(@Valid @RequestBody CategoryRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public CategoryResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody CategoryRequest req) {
        Category e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
