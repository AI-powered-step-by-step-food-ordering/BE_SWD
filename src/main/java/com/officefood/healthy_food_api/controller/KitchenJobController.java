package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.KitchenJobRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.KitchenJobResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.KitchenJobMapper;
import com.officefood.healthy_food_api.model.KitchenJob;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/kitchen_jobs")
@RequiredArgsConstructor
public class KitchenJobController {
    private final ServiceProvider sp;
    private final KitchenJobMapper mapper;

    // GET /api/kitchen_jobs/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<KitchenJobResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        List<KitchenJob> allJobs = sp.kitchenJobs().findAll();
        List<KitchenJob> sortedJobs = com.officefood.healthy_food_api.utils.SortUtils.sortEntities(allJobs, sortBy, sortDir);
        PagedResponse<KitchenJobResponse> pagedResponse = createPagedResponse(sortedJobs, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Kitchen jobs retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to create PagedResponse from KitchenJob list
     */
    private PagedResponse<KitchenJobResponse> createPagedResponse(List<KitchenJob> jobs, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = jobs.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<KitchenJobResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            pageContent = jobs.stream()
                    .skip(startIndex)
                    .limit(size)
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        return PagedResponse.<KitchenJobResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    // GET /api/kitchen_jobs/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<KitchenJobResponse>> getById(@PathVariable String id) {
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
    public ResponseEntity<ApiResponse<KitchenJobResponse>> update(@PathVariable String id,
                              @Valid @RequestBody KitchenJobRequest req) {
        KitchenJob entity = mapper.toEntity(req);
        entity.setId(id);
        KitchenJobResponse response = mapper.toResponse(sp.kitchenJobs().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Kitchen job updated successfully", response));
    }

    // DELETE /api/kitchen_jobs/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.kitchenJobs().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Kitchen job deleted successfully", null));
    }
}
