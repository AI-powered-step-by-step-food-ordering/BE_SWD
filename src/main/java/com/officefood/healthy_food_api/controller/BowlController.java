package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.BowlRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.BowlResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.BowlMapper;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bowls")
@RequiredArgsConstructor
public class BowlController {
    private final ServiceProvider sp;
    private final BowlMapper mapper;

    // GET /api/bowls/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<BowlResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<Bowl> allBowls = sp.bowls().findAllWithTemplateAndSteps();
        List<Bowl> sortedBowls = sortBowls(allBowls, sortBy, sortDir);
        PagedResponse<BowlResponse> pagedResponse = createPagedResponse(sortedBowls, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowls retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to sort bowls
     */
    private List<Bowl> sortBowls(List<Bowl> bowls, String sortBy, String sortDir) {
        if (bowls == null || bowls.isEmpty()) {
            return bowls;
        }

        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        try {
            Comparator<Bowl> comparator = (bowl1, bowl2) -> {
                try {
                    Object value1 = getFieldValue(bowl1, sortBy);
                    Object value2 = getFieldValue(bowl2, sortBy);

                    if (value1 == null && value2 == null) return 0;
                    if (value1 == null) return 1;
                    if (value2 == null) return -1;

                    int result = compareValues(value1, value2);
                    return ascending ? result : -result;
                } catch (Exception e) {
                    return 0;
                }
            };

            return bowls.stream().sorted(comparator).collect(Collectors.toList());
        } catch (Exception e) {
            return bowls;
        }
    }

    private Object getFieldValue(Bowl entity, String fieldName) throws Exception {
        Field field = findField(entity.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(entity);
        }
        return null;
    }

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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareValues(Object value1, Object value2) {
        if (value1 instanceof Comparable && value2 instanceof Comparable) {
            if (value1.getClass().equals(value2.getClass())) {
                return ((Comparable) value1).compareTo(value2);
            }
        }
        return value1.toString().compareTo(value2.toString());
    }

    /**
     * Helper method to create PagedResponse from Bowl list
     */
    private PagedResponse<BowlResponse> createPagedResponse(List<Bowl> bowls, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = bowls.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<BowlResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            pageContent = bowls.stream()
                    .skip(startIndex)
                    .limit(size)
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        return PagedResponse.<BowlResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    // GET /api/bowls/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<BowlResponse>> getById(@PathVariable String id) {
        return sp.bowls()
                 .findByIdWithTemplateAndSteps(id)
                 .map(mapper::toResponse)
                 .map(bowl -> ResponseEntity.ok(ApiResponse.success(200, "Bowl retrieved successfully", bowl)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl not found")));
    }

    // GET /api/bowls/getbyid/{id}/with-items - Get bowl with all items
    @GetMapping("/getbyid/{id}/with-items")
    public ResponseEntity<ApiResponse<BowlResponse>> getByIdWithItems(@PathVariable String id) {
        return sp.bowls()
                 .findByIdWithTemplateAndItems(id)
                 .map(mapper::toResponse)
                 .map(bowl -> ResponseEntity.ok(ApiResponse.success(200, "Bowl with items retrieved successfully", bowl)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Bowl not found")));
    }

    // POST /api/bowls/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BowlResponse>> create(@Valid @RequestBody BowlRequest req) {
        BowlResponse response = mapper.toResponse(sp.bowls().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Bowl created successfully", response));
    }

    // PUT /api/bowls/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<BowlResponse>> update(@PathVariable String id,
                              @Valid @RequestBody BowlRequest req) {
        Bowl entity = mapper.toEntity(req);
        entity.setId(id);
        BowlResponse response = mapper.toResponse(sp.bowls().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl updated successfully", response));
    }

    // DELETE /api/bowls/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.bowls().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Bowl deleted successfully", null));
    }
}
