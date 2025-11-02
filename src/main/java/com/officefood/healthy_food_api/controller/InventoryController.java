package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.InventoryRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.InventoryResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.InventoryMapper;
import com.officefood.healthy_food_api.model.Inventory;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final ServiceProvider sp;
    private final InventoryMapper mapper;

    // GET /api/inventories/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<InventoryResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<Inventory> allInventories = sp.inventories().findAll();
        PagedResponse<InventoryResponse> pagedResponse = createPagedResponse(allInventories, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Inventories retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to create PagedResponse from Inventory list
     */
    private PagedResponse<InventoryResponse> createPagedResponse(List<Inventory> inventories, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = inventories.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<InventoryResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            pageContent = inventories.stream()
                    .skip(startIndex)
                    .limit(size)
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        return PagedResponse.<InventoryResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    // GET /api/inventories/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getById(@PathVariable String id) {
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
    public ResponseEntity<ApiResponse<InventoryResponse>> update(@PathVariable String id,
                              @Valid @RequestBody InventoryRequest req) {
        Inventory entity = mapper.toEntity(req);
        entity.setId(id);
        InventoryResponse response = mapper.toResponse(sp.inventories().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Inventory updated successfully", response));
    }

    // DELETE /api/inventories/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.inventories().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Inventory deleted successfully", null));
    }
}
