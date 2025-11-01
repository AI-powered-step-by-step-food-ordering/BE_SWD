package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.controller.base.BaseController;
import com.officefood.healthy_food_api.dto.request.StoreRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.StoreResponse;
import com.officefood.healthy_food_api.mapper.StoreMapper;
import com.officefood.healthy_food_api.model.Store;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.CrudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController extends BaseController<Store, StoreRequest, StoreResponse> {
    private final ServiceProvider sp;
    private final StoreMapper mapper;

    @Override
    protected CrudService<Store> getService() {
        return sp.stores();
    }

    @Override
    protected StoreResponse toResponse(Store entity) {
        return mapper.toResponse(entity);
    }

    @Override
    protected Store toEntity(StoreRequest request) {
        return mapper.toEntity(request);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<StoreResponse>> create(@Valid @RequestBody StoreRequest req) {
        StoreResponse response = mapper.toResponse(sp.stores().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Store created successfully", response));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<StoreResponse>> update(@PathVariable String id,
                              @Valid @RequestBody StoreRequest req) {
        Store entity = mapper.toEntity(req);
        entity.setId(id);
        StoreResponse response = mapper.toResponse(sp.stores().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Store updated successfully", response));
    }
}
