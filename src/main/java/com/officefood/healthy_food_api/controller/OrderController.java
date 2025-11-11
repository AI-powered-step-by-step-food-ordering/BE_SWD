package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.OrderRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.OrderResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
import com.officefood.healthy_food_api.mapper.OrderMapper;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import com.officefood.healthy_food_api.service.TemplateStepEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final ServiceProvider sp;
    private final OrderMapper mapper;
    private final TemplateStepEnrichmentService enrichmentService;

    // GET /api/orders/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        // Giới hạn max size để tránh Cartesian Product explosion với JOIN FETCH bowls
        if (size > 30) {
            return ResponseEntity.ok(ApiResponse.error(400, "INVALID_SIZE",
                "Size cannot exceed 50 for orders (to prevent memory overflow due to JOIN FETCH on collections)"));
        }

        List<Order> allOrders = sp.orders().findAllWithBowlsAndUser();
        List<Order> sortedOrders = sortOrders(allOrders, sortBy, sortDir);
        PagedResponse<OrderResponse> pagedResponse = createPagedResponse(sortedOrders, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Orders retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to sort orders
     */
    private List<Order> sortOrders(List<Order> orders, String sortBy, String sortDir) {
        if (orders == null || orders.isEmpty()) return orders;
        boolean ascending = "asc".equalsIgnoreCase(sortDir);
        try {
            Comparator<Order> comparator = (o1, o2) -> {
                try {
                    Object v1 = getFieldValue(o1, sortBy);
                    Object v2 = getFieldValue(o2, sortBy);
                    if (v1 == null && v2 == null) return 0;
                    if (v1 == null) return 1;
                    if (v2 == null) return -1;
                    int result = compareValues(v1, v2);
                    return ascending ? result : -result;
                } catch (Exception e) {
                    return 0;
                }
            };
            return orders.stream().sorted(comparator).collect(Collectors.toList());
        } catch (Exception e) {
            return orders;
        }
    }

    private Object getFieldValue(Order entity, String fieldName) throws Exception {
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
    private int compareValues(Object v1, Object v2) {
        if (v1 instanceof Comparable && v2 instanceof Comparable) {
            if (v1.getClass().equals(v2.getClass())) {
                return ((Comparable) v1).compareTo(v2);
            }
        }
        return v1.toString().compareTo(v2.toString());
    }

    /**
     * Helper method to create PagedResponse from Order list
     */
    private PagedResponse<OrderResponse> createPagedResponse(List<Order> orders, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = orders.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<OrderResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            List<Order> pageOrders = orders.stream()
                    .skip(startIndex)
                    .limit(size)
                    .collect(Collectors.toList());

            pageContent = pageOrders.stream()
                    .map(entity -> {
                        OrderResponse response = mapper.toResponse(entity);
                        enrichOrderResponse(response, entity);
                        return response;
                    })
                    .collect(Collectors.toList());
        }

        return PagedResponse.<OrderResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    /**
     * Helper method to enrich OrderResponse with ingredient details in defaultIngredients
     */
    private void enrichOrderResponse(OrderResponse response, Order entity) {
        if (response.getBowls() == null || entity.getBowls() == null) {
            return;
        }

        // Create a map of entity bowls by ID for quick lookup
        java.util.Map<String, com.officefood.healthy_food_api.model.Bowl> entityBowlMap =
            entity.getBowls().stream()
                .collect(java.util.stream.Collectors.toMap(
                    com.officefood.healthy_food_api.model.Bowl::getId,
                    b -> b
                ));

        // Enrich each bowl's template steps
        for (var bowlResponse : response.getBowls()) {
            var bowlEntity = entityBowlMap.get(bowlResponse.getId());

            if (bowlEntity == null) continue;

            if (bowlResponse.getTemplate() != null &&
                bowlResponse.getTemplate().getSteps() != null &&
                bowlEntity.getTemplate() != null &&
                bowlEntity.getTemplate().getSteps() != null) {

                // Enrich each template step's defaultIngredients
                bowlEntity.getTemplate().getSteps().forEach(step -> {
                    bowlResponse.getTemplate().getSteps().stream()
                        .filter(stepRes -> stepRes.getId().equals(step.getId()))
                        .findFirst()
                        .ifPresent(stepRes -> enrichmentService.enrichDefaultIngredients(stepRes, step));
                });
            }
        }
    }

    // GET /api/orders/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable String id) {
        return sp.orders()
                 .findByIdWithBowlsAndUser(id)
                 .map(entity -> {
                     OrderResponse response = mapper.toResponse(entity);
                     enrichOrderResponse(response, entity);
                     return response;
                 })
                 .map(order -> ResponseEntity.ok(ApiResponse.success(200, "Order retrieved successfully", order)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Order not found")));
    }

    // POST /api/orders/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest req) {
        // Create order
        Order created = sp.orders().create(mapper.toEntity(req));

        // Reload with user joined to populate userFullName
        Order enriched = sp.orders().findByIdWithBowlsAndUser(created.getId())
            .orElse(created); // Fallback to created if reload fails

        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(201, "Order created successfully", response));
    }

    // PUT /api/orders/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(@PathVariable String id,
                              @Valid @RequestBody OrderRequest req) {
        Order entity = mapper.toEntity(req);
        entity.setId(id);

        // Update order
        Order updated = sp.orders().update(id, entity);

        // Reload with user joined to populate userFullName
        Order enriched = sp.orders().findByIdWithBowlsAndUser(updated.getId())
            .orElse(updated); // Fallback to updated if reload fails

        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Order updated successfully", response));
    }

    // DELETE /api/orders/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.orders().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Order deleted successfully", null));
    }

    // POST /api/orders/recalc/{id}
    @PostMapping("/recalc/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> recalc(@PathVariable String id) {
        Order recalculated = sp.orders().recalcTotals(id);
        Order enriched = sp.orders().findByIdWithBowlsAndUser(recalculated.getId()).orElse(recalculated);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Order totals recalculated successfully", response));
    }

    // POST /api/orders/apply-promo/{id}?code=...
    @PostMapping("/apply-promo/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> applyPromo(@PathVariable String id, @RequestParam String code) {
        Order withPromo = sp.orders().applyPromotion(id, code);
        Order enriched = sp.orders().findByIdWithBowlsAndUser(withPromo.getId()).orElse(withPromo);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion applied successfully", response));
    }

    // POST /api/orders/confirm/{id}
    @PostMapping("/confirm/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(@PathVariable String id) {
        Order confirmed = sp.orders().confirm(id);
        Order enriched = sp.orders().findByIdWithBowlsAndUser(confirmed.getId()).orElse(confirmed);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Order confirmed successfully", response));
    }

    // POST /api/orders/cancel/{id}?reason=...
    @PostMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable String id, @RequestParam(required=false) String reason) {
        Order cancelled = sp.orders().cancel(id, reason);
        Order enriched = sp.orders().findByIdWithBowlsAndUser(cancelled.getId()).orElse(cancelled);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Order cancelled successfully", response));
    }

    // POST /api/orders/complete/{id}
    @PostMapping("/complete/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> complete(@PathVariable String id) {
        Order completed = sp.orders().complete(id);
        Order enriched = sp.orders().findByIdWithBowlsAndUser(completed.getId()).orElse(completed);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Order completed successfully", response));
    }

    // PUT /api/orders/{orderId}/status - Update order status with push notification
    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String orderId,
            @Valid @RequestBody com.officefood.healthy_food_api.dto.request.UpdateOrderStatusRequest request) {
        Order order = sp.orders().findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Update status
        order.setStatus(request.getStatus());
        Order updatedOrder = sp.orders().update(orderId, order);

        // Send push notification
        try {
            sp.fcm().sendOrderNotification(updatedOrder, request.getStatus());
        } catch (Exception e) {
            // Log but don't fail the request if notification fails
            System.err.println("Failed to send notification: " + e.getMessage());
        }

        // Reload with user joined to populate userFullName
        Order enriched = sp.orders().findByIdWithBowlsAndUser(updatedOrder.getId()).orElse(updatedOrder);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Order status updated successfully", response));
    }

    // GET /api/orders/order-history/{userId} - Get order history by user ID with pagination and sorting
    @GetMapping("/order-history/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate userId format (should be UUID)
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error(400, "INVALID_USER_ID",
                "User ID cannot be empty"));
        }

        // Check if user exists
        boolean userExists = sp.users().findById(userId).isPresent();
        if (!userExists) {
            return ResponseEntity.ok(ApiResponse.error(404, "USER_NOT_FOUND",
                "User not found with ID: " + userId + ". Please use valid UUID format."));
        }

        // Giới hạn max size để tránh memory overflow khi load order với bowls và templates
        if (size > 20) {
            return ResponseEntity.ok(ApiResponse.error(400, "INVALID_SIZE",
                "Size cannot exceed 20 for order history (to prevent memory overflow due to nested JOIN FETCH)"));
        }

        try {
            List<Order> userOrders = sp.orders().findByUserIdWithBowlsAndUser(userId);
            List<Order> sortedOrders = sortOrders(userOrders, sortBy, sortDir);
            PagedResponse<OrderResponse> pagedResponse = createPagedResponse(sortedOrders, page, size);
            return ResponseEntity.ok(ApiResponse.success(200, "Order history retrieved successfully", pagedResponse));
        } catch (Exception e) {
            // Log error
            System.err.println("Error fetching order history for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(500, "FETCH_ERROR",
                "Failed to fetch order history: " + e.getMessage()));
        }
    }
}
