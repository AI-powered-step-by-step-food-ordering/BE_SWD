package com.officefood.healthy_food_api.controller.base;

import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.model.BaseEntity;
import com.officefood.healthy_food_api.service.CrudService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.Comparator;
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
     * GET /getall - Lấy tất cả (bao gồm cả active và inactive) với phân trang và sorting
     * @param page Số trang (bắt đầu từ 0), mặc định = 0
     * @param size Số item mỗi trang, mặc định = 5
     * @param sortBy Field để sort (mặc định = "createdAt")
     * @param sortDir Direction để sort: "asc" hoặc "desc" (mặc định = "desc")
     */
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<RES>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<T> allEntities = getService().findAll();
        List<T> sortedEntities = sortEntities(allEntities, sortBy, sortDir);
        PagedResponse<RES> pagedResponse = createPagedResponse(sortedEntities, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved all records successfully", pagedResponse));
    }

    /**
     * GET /active - Chỉ lấy các records active (isActive = true) với phân trang và sorting
     * @param page Số trang (bắt đầu từ 0), mặc định = 0
     * @param size Số item mỗi trang, mặc định = 5
     * @param sortBy Field để sort (mặc định = "createdAt")
     * @param sortDir Direction để sort: "asc" hoặc "desc" (mặc định = "desc")
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagedResponse<RES>>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<T> activeEntities = getService()
                .findAll()
                .stream()
                .filter(BaseEntity::getIsActive)
                .collect(Collectors.toList());
        List<T> sortedEntities = sortEntities(activeEntities, sortBy, sortDir);
        PagedResponse<RES> pagedResponse = createPagedResponse(sortedEntities, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved active records successfully", pagedResponse));
    }

    /**
     * GET /inactive - Chỉ lấy các records inactive (isActive = false) với phân trang và sorting
     * @param page Số trang (bắt đầu từ 0), mặc định = 0
     * @param size Số item mỗi trang, mặc định = 5
     * @param sortBy Field để sort (mặc định = "createdAt")
     * @param sortDir Direction để sort: "asc" hoặc "desc" (mặc định = "desc")
     */
    @GetMapping("/inactive")
    public ResponseEntity<ApiResponse<PagedResponse<RES>>> getAllInactive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<T> inactiveEntities = getService()
                .findAll()
                .stream()
                .filter(entity -> !entity.getIsActive())
                .collect(Collectors.toList());
        List<T> sortedEntities = sortEntities(inactiveEntities, sortBy, sortDir);
        PagedResponse<RES> pagedResponse = createPagedResponse(sortedEntities, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Retrieved inactive records successfully", pagedResponse));
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
     * DELETE /delete/{id} - Hard delete (xóa vĩnh viễn)
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> hardDelete(@PathVariable String id) {
        getService().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Record deleted permanently", null));
    }

    /**
     * Helper method để sort entities theo field và direction
     */
    protected List<T> sortEntities(List<T> entities, String sortBy, String sortDir) {
        if (entities == null || entities.isEmpty()) {
            return entities;
        }

        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        try {
            Comparator<T> comparator = (entity1, entity2) -> {
                try {
                    Object value1 = getFieldValue(entity1, sortBy);
                    Object value2 = getFieldValue(entity2, sortBy);

                    // Handle null values - nulls last for both directions
                    if (value1 == null && value2 == null) return 0;
                    if (value1 == null) return 1;
                    if (value2 == null) return -1;

                    // Compare based on type
                    int result = compareValues(value1, value2);
                    return ascending ? result : -result;
                } catch (Exception e) {
                    return 0; // If comparison fails, consider them equal
                }
            };

            return entities.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // If sorting fails, return original list
            return entities;
        }
    }

    /**
     * Get field value from entity using reflection
     */
    private Object getFieldValue(T entity, String fieldName) throws Exception {
        try {
            // Try to get field from entity class
            Field field = findField(entity.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(entity);
            }
        } catch (Exception e) {
            // Field not found or not accessible
        }
        return null;
    }

    /**
     * Find field in class hierarchy
     */
    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Compare two values based on their type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareValues(Object value1, Object value2) {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            if (value1.getClass().equals(value2.getClass())) {
                return ((Comparable) value1).compareTo(value2);
            }
        }

        // Fallback to string comparison
        return value1.toString().compareTo(value2.toString());
    }

    /**
     * Helper method để tạo PagedResponse từ danh sách entities
     */
    protected PagedResponse<RES> createPagedResponse(List<T> entities, int page, int size) {
        // Đảm bảo size tối thiểu là 1
        if (size < 1) {
            size = 5;
        }

        // Đảm bảo page không âm
        if (page < 0) {
            page = 0;
        }

        // Tính toán pagination
        int totalElements = entities.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<RES> pageContent;
        if (page >= totalPages && totalPages > 0) {
            // Page vượt quá số trang có data -> trả về empty list
            pageContent = List.of();
        } else {
            // Lấy subset của entities cho page hiện tại
            int startIndex = page * size;
            pageContent = entities.stream()
                    .skip(startIndex)
                    .limit(size)
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }

        return PagedResponse.<RES>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }
}

