package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.OrderRequest;
import com.officefood.healthy_food_api.dto.request.OrderSearchRequest;
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
    
    // GET /api/orders/search - Simplified search endpoint (userId and storeId only)
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> search(
            @ModelAttribute OrderSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        

        // Execute search
        List<Order> orders = sp.orders().search(searchRequest);
        
        // Apply sorting
        List<Order> sortedOrders = sortOrders(orders, sortBy, sortDir);
        
        // Create paged response
        PagedResponse<OrderResponse> pagedResponse = createPagedResponse(sortedOrders, page, size);
        
        return ResponseEntity.ok(ApiResponse.success(200, 
            "Orders search completed successfully. Found " + orders.size() + " results.", 
            pagedResponse));
    }

    // GET /api/orders/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            System.out.println(String.format("getAll called with: page=%d, size=%d, sortBy=%s, sortDir=%s", page, size, sortBy, sortDir));

            // Giới hạn max size để tránh Cartesian Product explosion với JOIN FETCH bowls
            if (size > 30) {
                return ResponseEntity.ok(ApiResponse.error(400, "INVALID_SIZE",
                    "Size cannot exceed 30 for orders (to prevent memory overflow due to JOIN FETCH on collections)"));
            }

            System.out.println("Fetching all orders with bowls and user...");
            List<Order> allOrders = sp.orders().findAllWithBowlsAndUser();
            System.out.println("Found " + (allOrders != null ? allOrders.size() : 0) + " orders");

            System.out.println("Sorting orders...");
            List<Order> sortedOrders = sortOrders(allOrders, sortBy, sortDir);
            System.out.println("Sorting completed");

            System.out.println("Creating paged response...");
            PagedResponse<OrderResponse> pagedResponse = createPagedResponse(sortedOrders, page, size);
            System.out.println("Paged response created successfully");

            return ResponseEntity.ok(ApiResponse.success(200, "Orders retrieved successfully", pagedResponse));
        } catch (Exception e) {
            System.err.println("Error in getAll: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(ApiResponse.error(9999, "UNCATEGORIZED_EXCEPTION", "Internal server error"));
        }
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
        try {
            if (size < 1) size = 5;
            if (page < 0) page = 0;

            int totalElements = orders == null ? 0 : orders.size();
            int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

            // Log pagination details for debugging
            System.out.println(String.format("Pagination Debug: page=%d, size=%d, totalElements=%d, totalPages=%d",
                page, size, totalElements, totalPages));

            // Handle empty orders list
            if (totalElements == 0) {
                return PagedResponse.<OrderResponse>builder()
                        .content(List.of())
                        .page(0)
                        .size(size)
                        .totalElements(0)
                        .totalPages(0)
                        .first(true)
                        .last(true)
                        .build();
            }

            // Calculate bounds
            int startIndex = page * size;

            // If page is beyond available data, return empty content but valid pagination info
            List<OrderResponse> pageContent;
            if (startIndex >= totalElements) {
                System.out.println(String.format("Page beyond data: startIndex=%d >= totalElements=%d", startIndex, totalElements));
                pageContent = List.of();
            } else {
                try {
                    // Get page slice with bounds checking
                    List<Order> pageOrders = orders.stream()
                            .skip(startIndex)
                            .limit(size)
                            .collect(Collectors.toList());

                    System.out.println(String.format("Processing %d orders from index %d", pageOrders.size(), startIndex));

                    // Map to responses with individual error handling
                    pageContent = pageOrders.stream()
                            .map(entity -> {
                                if (entity == null) {
                                    System.err.println("Warning: Null order entity found");
                                    return null;
                                }

                                try {
                                    OrderResponse response = mapper.toResponse(entity);
                                    // Try enrichment, but don't fail if it throws exception
                                    try {
                                        enrichOrderResponse(response, entity);
                                    } catch (Exception enrichError) {
                                        System.err.println("Enrichment failed for order " + entity.getId() + ": " + enrichError.getMessage());
                                        // Continue with basic response without enrichment
                                    }
                                    return response;
                                } catch (org.hibernate.LazyInitializationException lazyError) {
                                    // Handle lazy initialization specifically - create minimal response
                                    System.err.println("LazyInitializationException for order " + entity.getId() + ", creating minimal response");
                                    try {
                                        return createMinimalOrderResponse(entity);
                                    } catch (Exception fallbackError) {
                                        System.err.println("Even minimal response creation failed for order " + entity.getId() + ": " + fallbackError.getMessage());
                                        return null;
                                    }
                                } catch (Exception mappingError) {
                                    System.err.println("Mapping failed for order " + (entity.getId() != null ? entity.getId() : "unknown") + ": " + mappingError.getMessage());
                                    // Try minimal response as fallback
                                    try {
                                        return createMinimalOrderResponse(entity);
                                    } catch (Exception fallbackError) {
                                        System.err.println("Fallback response creation failed for order " + (entity.getId() != null ? entity.getId() : "unknown") + ": " + fallbackError.getMessage());
                                        return null;
                                    }
                                }
                            })
                            .filter(response -> response != null)
                            .collect(Collectors.toList());

                    System.out.println(String.format("Successfully mapped %d/%d orders", pageContent.size(), pageOrders.size()));
                } catch (Exception streamError) {
                    System.err.println("Stream processing error: " + streamError.getMessage());
                    streamError.printStackTrace();
                    pageContent = List.of();
                }
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

        } catch (Exception e) {
            System.err.println("Fatal error in createPagedResponse: " + e.getMessage());
            e.printStackTrace();

            // Return safe fallback response
            return PagedResponse.<OrderResponse>builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(0)
                    .totalPages(0)
                    .first(true)
                    .last(true)
                    .build();
        }
    }

    /**
     * Helper method to enrich OrderResponse with ingredient details in defaultIngredients
     */
    private void enrichOrderResponse(OrderResponse response, Order entity) {
        if (response == null || entity == null) {
            return;
        }

        try {
            // Check if bowls are present and initialized
            if (response.getBowls() == null || response.getBowls().isEmpty()) {
                return;
            }

            if (entity.getBowls() == null || entity.getBowls().isEmpty()) {
                return;
            }

            // Check if Hibernate collection is initialized to avoid LazyInitializationException
            try {
                if (!org.hibernate.Hibernate.isInitialized(entity.getBowls())) {
                    System.err.println("Warning: Bowls collection not initialized for order " + entity.getId());
                    return;
                }
            } catch (Exception hibernateCheck) {
                // If Hibernate is not available or fails, continue with caution
                System.err.println("Could not check Hibernate initialization, proceeding with caution");
            }

            // Create a map of entity bowls by ID for quick lookup with comprehensive null safety
            java.util.Map<String, com.officefood.healthy_food_api.model.Bowl> entityBowlMap;
            try {
                entityBowlMap = entity.getBowls().stream()
                    .filter(b -> {
                        if (b == null) {
                            System.err.println("Warning: Found null bowl in order " + entity.getId());
                            return false;
                        }
                        if (b.getId() == null) {
                            System.err.println("Warning: Found bowl with null ID in order " + entity.getId());
                            return false;
                        }
                        return true;
                    })
                    .collect(java.util.stream.Collectors.toMap(
                        com.officefood.healthy_food_api.model.Bowl::getId,
                        b -> b,
                        (b1, b2) -> {
                            System.err.println("Warning: Duplicate bowl ID " + b1.getId() + " in order " + entity.getId());
                            return b1; // Keep first if duplicate
                        }
                    ));
            } catch (Exception mapCreationError) {
                System.err.println("Error creating bowl map for order " + entity.getId() + ": " + mapCreationError.getMessage());
                return;
            }

            // Enrich each bowl's template steps
            for (var bowlResponse : response.getBowls()) {
                if (bowlResponse == null || bowlResponse.getId() == null) {
                    continue;
                }

                var bowlEntity = entityBowlMap.get(bowlResponse.getId());
                if (bowlEntity == null) {
                    continue;
                }

                // Check template and steps existence with null safety
                if (bowlResponse.getTemplate() == null ||
                    bowlResponse.getTemplate().getSteps() == null ||
                    bowlEntity.getTemplate() == null ||
                    bowlEntity.getTemplate().getSteps() == null) {
                    continue;
                }

                // Check if template steps are initialized
                try {
                    if (!org.hibernate.Hibernate.isInitialized(bowlEntity.getTemplate().getSteps())) {
                        System.err.println("Warning: Template steps not initialized for bowl " + bowlEntity.getId());
                        continue;
                    }
                } catch (Exception stepCheck) {
                    // If check fails, proceed with caution
                }

                // Enrich each template step's defaultIngredients with comprehensive error handling
                try {
                    bowlEntity.getTemplate().getSteps().forEach(step -> {
                        if (step == null || step.getId() == null) {
                            return;
                        }

                        try {
                            bowlResponse.getTemplate().getSteps().stream()
                                .filter(stepRes -> stepRes != null &&
                                               stepRes.getId() != null &&
                                               stepRes.getId().equals(step.getId()))
                                .findFirst()
                                .ifPresent(stepRes -> {
                                    try {
                                        enrichmentService.enrichDefaultIngredients(stepRes, step);
                                    } catch (Exception enrichError) {
                                        // Silently skip individual enrichment failures
                                        System.err.println("Individual enrichment failed for step " + step.getId() + ": " + enrichError.getMessage());
                                    }
                                });
                        } catch (Exception stepProcessingError) {
                            // Silently skip if individual step processing fails
                            System.err.println("Step processing failed for step " + step.getId() + ": " + stepProcessingError.getMessage());
                        }
                    });
                } catch (Exception stepsIterationError) {
                    System.err.println("Steps iteration failed for bowl " + bowlEntity.getId() + ": " + stepsIterationError.getMessage());
                }
            }
        } catch (Exception e) {
            // Log but don't fail - enrichment is optional
            System.err.println("Error enriching order " + (entity.getId() != null ? entity.getId() : "unknown") + ": " + e.getMessage());
            e.printStackTrace();
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

    // DELETE /api/orders/remove-promo/{id}
    @DeleteMapping("/remove-promo/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> removePromo(@PathVariable String id) {
        Order withoutPromo = sp.orders().removePromotion(id);
        Order enriched = sp.orders().findByIdWithBowlsAndUser(withoutPromo.getId()).orElse(withoutPromo);
        OrderResponse response = mapper.toResponse(enriched);
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion removed successfully", response));
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

    /**
     * Helper method to create minimal OrderResponse when full mapping fails
     */
    private OrderResponse createMinimalOrderResponse(Order entity) {
        OrderResponse response = new OrderResponse();

        // Set basic fields that don't require proxy initialization
        response.setId(entity.getId());
        response.setStatus(entity.getStatus() != null ? entity.getStatus().toString() : null);
        response.setSubtotalAmount(entity.getSubtotalAmount());
        response.setPromotionTotal(entity.getPromotionTotal());
        response.setTotalAmount(entity.getTotalAmount());
        response.setCreatedAt(entity.getCreatedAt());

        // Try to get userId safely - use the User relationship
        try {
            if (entity.getUser() != null && entity.getUser().getId() != null) {
                response.setUserId(entity.getUser().getId());
            } else {
                response.setUserId(null);
            }
        } catch (Exception e) {
            response.setUserId(null);
        }

        // Try to get storeId safely - use the Store relationship
        try {
            if (entity.getStore() != null && entity.getStore().getId() != null) {
                response.setStoreId(entity.getStore().getId());
            } else {
                response.setStoreId(null);
            }
        } catch (Exception e) {
            response.setStoreId(null);
        }

        // Try to get userFullName safely
        try {
            if (entity.getUser() != null && org.hibernate.Hibernate.isInitialized(entity.getUser())) {
                response.setUserFullName(entity.getUser().getFullName());
            } else {
                response.setUserFullName("N/A (lazy load failed)");
            }
        } catch (Exception e) {
            response.setUserFullName("N/A (error loading user)");
        }

        // Set empty bowls list to avoid null issues
        response.setBowls(List.of());

        return response;
    }
}
