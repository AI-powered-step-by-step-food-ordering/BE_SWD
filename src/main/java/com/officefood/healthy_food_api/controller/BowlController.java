package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlResponse;
import com.officefood.healthy_food_api.mapper.BowlMapper;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bowls")
@RequiredArgsConstructor
public class BowlController {
    private final ServiceProvider sp;
    private final BowlMapper mapper;

    // GET /api/bowls/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<BowlResponse>>> getAll() {
        List<BowlResponse> bowls = sp.bowls()
                 .findAllWithTemplateAndSteps()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Bowls retrieved successfully", bowls));
    }

    // GET /api/bowls/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlResponse>> getById(@PathVariable String id) {
        return sp.bowls()
                 .findByIdWithTemplateAndSteps(id)
                 .map(mapper::toResponse)
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
}
