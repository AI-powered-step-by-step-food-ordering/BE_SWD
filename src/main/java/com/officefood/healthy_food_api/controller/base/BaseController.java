package com.officefood.healthy_food_api.controller.base;

import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.model.BaseEntity;
import com.officefood.healthy_food_api.service.CrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base Controller vÃ¡Â»â€ºi cÃƒÂ¡c methods chuÃ¡ÂºÂ©n cho soft delete
 * @param <T> Entity type extends BaseEntity
 * @param <REQ> Request DTO type
 * @param <RES> Response DTO type
 */
public abstract class BaseController<T extends BaseEntity, REQ, RES> {

    protected abstract CrudService<T> getService();

    protected abstract RES toResponse(T entity);

    protected abstract T toEntity(REQ request);

    /**
     * GET /getall - LÃ¡ÂºÂ¥y tÃ¡ÂºÂ¥t cÃ¡ÂºÂ£ (bao gÃ¡Â»â€œm cÃ¡ÂºÂ£ active vÃƒÂ  inactive)
     */
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<RES>>> getAll() {
        List<RES> responses = getService()
                .findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved all records successfully", responses));
    }

    /**
     * GET /active - ChÃ¡Â»â€° lÃ¡ÂºÂ¥y cÃƒÂ¡c records active (isActive = true)
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<RES>>> getAllActive() {
        List<RES> responses = getService()
                .findAll()
                .stream()
                .filter(BaseEntity::getIsActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved active records successfully", responses));
    }

    /**
     * GET /inactive - ChÃ¡Â»â€° lÃ¡ÂºÂ¥y cÃƒÂ¡c records inactive (isActive = false)
     */
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<List<RES>>> getAllInactive() {
        List<RES> responses = getService()
                .findAll()
                .stream()
                .filter(entity -> !entity.getIsActive())
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved inactive records successfully", responses));
    }

    /**
     * GET /getbyid/{id} - ChÃ¡Â»â€° lÃ¡ÂºÂ¥y record active
     */
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<RES>> getById(@PathVariable String id) {
        return getService()
                .findById(id)
                .filter(BaseEntity::getIsActive)
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(200, "Record retrieved successfully", response)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found or inactive")));
    }

    /**
     * GET /getbyid-include-deleted/{id} - LÃ¡ÂºÂ¥y record kÃ¡Â»Æ’ cÃ¡ÂºÂ£ Ã„â€˜ÃƒÂ£ xÃƒÂ³a mÃ¡Â»Âm
     */
    @GetMapping("/getbyid-include-deleted/{id}")
    public ResponseEntity<ApiResponse<RES>> getByIdIncludeDeleted(@PathVariable String id) {
        return getService()
                .findById(id)
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(200, "Record retrieved successfully", response)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found")));
    }

    /**
     * PUT /soft-delete/{id} - XÃƒÂ³a mÃ¡Â»Âm
     */
    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable String id) {
        try {
            getService().softDelete(id);
            return ResponseEntity.ok(ApiResponse.<Void>success(200, "Record soft deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found"));
        }
    }

    /**
     * PUT /restore/{id} - KhÃƒÂ´i phÃ¡Â»Â¥c record Ã„â€˜ÃƒÂ£ xÃƒÂ³a mÃ¡Â»Âm
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable String id) {
        try {
            getService().restore(id);
            return ResponseEntity.ok(ApiResponse.<Void>success(200, "Record restored successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found"));
        }
    }

    /**
     * DELETE /delete/{id} - Hard delete (xÃƒÂ³a vÃ„Â©nh viÃ¡Â»â€¦n)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable String id) {
        getService().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Record deleted permanently", null));
    }
}

