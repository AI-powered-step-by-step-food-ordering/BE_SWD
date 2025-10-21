package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.KitchenJobRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.KitchenJobResponse;
import com.officefood.healthy_food_api.mapper.KitchenJobMapper;
import com.officefood.healthy_food_api.model.KitchenJob;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kitchen_jobs")
@RequiredArgsConstructor
public class KitchenJobController {
    private final ServiceProvider sp;
    private final KitchenJobMapper mapper;

    // GET /api/kitchen_jobs/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<KitchenJobResponse>>> getAll() {
        List<KitchenJobResponse> kitchenJobs = sp.kitchenJobs()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Kitchen jobs retrieved successfully", kitchenJobs));
    }

    // GET /api/kitchen_jobs/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<KitchenJobResponse>> getById(@PathVariable UUID id) {
        return sp.kitchenJobs()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(kitchenJob -> ResponseEntity.ok(ApiResponse.success(200, "Kitchen job retrieved successfully", kitchenJob)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Kitchen job not found")));
    }

    // POST /api/kitchen_jobs/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<KitchenJobResponse>> create(@Valid @RequestBody KitchenJobRequest req) {
        KitchenJobResponse response = mapper.toResponse(sp.kitchenJobs().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Kitchen job created successfully", response));
    }

    // PUT /api/kitchen_jobs/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<KitchenJobResponse>> update(@PathVariable UUID id,
                              @Valid @RequestBody KitchenJobRequest req) {
        KitchenJob entity = mapper.toEntity(req);
        entity.setId(id);
        KitchenJobResponse response = mapper.toResponse(sp.kitchenJobs().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Kitchen job updated successfully", response));
    }

    // DELETE /api/kitchen_jobs/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        sp.kitchenJobs().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Kitchen job deleted successfully", null));
    }
}
