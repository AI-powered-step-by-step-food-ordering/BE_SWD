package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.InventoryRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.InventoryResponse;
import com.officefood.healthy_food_api.mapper.InventoryMapper;
import com.officefood.healthy_food_api.model.Inventory;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final ServiceProvider sp;
    private final InventoryMapper mapper;

    // GET /api/inventories/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAll() {
        List<InventoryResponse> inventories = sp.inventories()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Inventories retrieved successfully", inventories));
    }

    // GET /api/inventories/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getById(@PathVariable UUID id) {
        return sp.inventories()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(inventory -> ResponseEntity.ok(ApiResponse.success(200, "Inventory retrieved successfully", inventory)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Inventory not found")));
    }

    // POST /api/inventories/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InventoryResponse>> create(@Valid @RequestBody InventoryRequest req) {
        InventoryResponse response = mapper.toResponse(sp.inventories().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Inventory created successfully", response));
    }

    // PUT /api/inventories/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody InventoryRequest req) {
        Inventory entity = mapper.toEntity(req);
        entity.setId(id);
        InventoryResponse response = mapper.toResponse(sp.inventories().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Inventory updated successfully", response));
    }

    // DELETE /api/inventories/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.inventories().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Inventory deleted successfully", null));
    }
}
