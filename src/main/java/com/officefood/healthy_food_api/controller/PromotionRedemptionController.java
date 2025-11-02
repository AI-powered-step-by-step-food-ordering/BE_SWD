package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.PromotionRedemptionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.dto.response.PromotionRedemptionResponse;
import com.officefood.healthy_food_api.mapper.PromotionRedemptionMapper;
import com.officefood.healthy_food_api.model.PromotionRedemption;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/promotion_redemptions")
@RequiredArgsConstructor
public class PromotionRedemptionController {
    private final ServiceProvider sp;
    private final PromotionRedemptionMapper mapper;

    // GET /api/promotion_redemptions/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<PromotionRedemptionResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        List<PromotionRedemption> allRedemptions = sp.promotionRedemptions().findAll();
        PagedResponse<PromotionRedemptionResponse> pagedResponse = createPagedResponse(allRedemptions, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion redemptions retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to create PagedResponse from PromotionRedemption list
     */
    private PagedResponse<PromotionRedemptionResponse> createPagedResponse(List<PromotionRedemption> redemptions, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = redemptions.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<PromotionRedemptionResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            pageContent = redemptions.stream()
                    .skip(startIndex)
                    .limit(size)
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        return PagedResponse.<PromotionRedemptionResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    // GET /api/promotion_redemptions/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<PromotionRedemptionResponse>> getById(@PathVariable String id) {
        return sp.promotionRedemptions()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(promotionRedemption -> ResponseEntity.ok(ApiResponse.success(200, "Promotion redemption retrieved successfully", promotionRedemption)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Promotion redemption not found")));
    }

    // POST /api/promotion_redemptions/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PromotionRedemptionResponse>> create(@Valid @RequestBody PromotionRedemptionRequest req) {
        PromotionRedemptionResponse response = mapper.toResponse(sp.promotionRedemptions().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Promotion redemption created successfully", response));
    }

    // PUT /api/promotion_redemptions/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PromotionRedemptionResponse>> update(@PathVariable String id,
                              @Valid @RequestBody PromotionRedemptionRequest req) {
        PromotionRedemption entity = mapper.toEntity(req);
        entity.setId(id);
        PromotionRedemptionResponse response = mapper.toResponse(sp.promotionRedemptions().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion redemption updated successfully", response));
    }

    // DELETE /api/promotion_redemptions/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.promotionRedemptions().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion redemption deleted successfully", null));
    }
}
