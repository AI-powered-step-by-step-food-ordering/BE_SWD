package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.StoreRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.StoreResponse;
import com.officefood.healthy_food_api.mapper.StoreMapper;
import com.officefood.healthy_food_api.model.Store;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
    private final ServiceProvider sp;
    private final StoreMapper mapper;

    // GET /api/stores/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getAll() {
        List<StoreResponse> stores = sp.stores()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Stores retrieved successfully", stores));
    }

    // GET /api/stores/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> getById(@PathVariable UUID id) {
        return sp.stores()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(store -> ResponseEntity.ok(ApiResponse.success(200, "Store retrieved successfully", store)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Store not found")));
    }

    // POST /api/stores/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StoreResponse>> create(@Valid @RequestBody StoreRequest req) {
        StoreResponse response = mapper.toResponse(sp.stores().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Store created successfully", response));
    }

    // PUT /api/stores/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody StoreRequest req) {
        Store entity = mapper.toEntity(req);
        entity.setId(id);
        StoreResponse response = mapper.toResponse(sp.stores().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Store updated successfully", response));
    }

    // DELETE /api/stores/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.stores().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Store deleted successfully", null));
    }
}
