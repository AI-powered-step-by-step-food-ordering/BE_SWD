package com.officefood.healthy_food_api.controller.base;

import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.model.BaseEntity;
import com.officefood.healthy_food_api.service.CrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Base Controller với các methods chuẩn cho soft delete
 * @param <T> Entity type extends BaseEntity
 * @param <REQ> Request DTO type
 * @param <RES> Response DTO type
 */
public abstract class BaseController<T extends BaseEntity, REQ, RES> {

    protected abstract CrudService<T> getService();

    protected abstract RES toResponse(T entity);

    protected abstract T toEntity(REQ request);

    /**
     * GET /getall - Lấy tất cả (bao gồm cả active và inactive)
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
     * GET /active - Chỉ lấy các records active (isActive = true)
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
     * GET /inactive - Chỉ lấy các records inactive (isActive = false)
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
     * GET /getbyid/{id} - Chỉ lấy record active
     */
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<RES>> getById(@PathVariable UUID id) {
        return getService()
                .findById(id)
                .filter(BaseEntity::getIsActive)
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(200, "Record retrieved successfully", response)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found or inactive")));
    }

    /**
     * GET /getbyid-include-deleted/{id} - Lấy record kể cả đã xóa mềm
     */
    @GetMapping("/getbyid-include-deleted/{id}")
    public ResponseEntity<ApiResponse<RES>> getByIdIncludeDeleted(@PathVariable UUID id) {
        return getService()
                .findById(id)
                .map(this::toResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(200, "Record retrieved successfully", response)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found")));
    }

    /**
     * PUT /soft-delete/{id} - Xóa mềm
     */
    @PutMapping("/soft-delete/{id}")
    public ResponseEntity<ApiResponse<Void>> softDelete(@PathVariable UUID id) {
        return getService()
                .findById(id)
                .map(entity -> {
                    entity.softDelete();
                    getService().update(id, entity);
                    return ResponseEntity.ok(ApiResponse.<Void>success(200, "Record soft deleted successfully", null));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found")));
    }

    /**
     * PUT /restore/{id} - Khôi phục record đã xóa mềm
     */
    @PutMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<Void>> restore(@PathVariable UUID id) {
        return getService()
                .findById(id)
                .map(entity -> {
                    entity.restore();
                    getService().update(id, entity);
                    return ResponseEntity.ok(ApiResponse.<Void>success(200, "Record restored successfully", null));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Record not found")));
    }

    /**
     * DELETE /delete/{id} - Hard delete (xóa vĩnh viễn)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable UUID id) {
        getService().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Record deleted permanently", null));
    }
}

