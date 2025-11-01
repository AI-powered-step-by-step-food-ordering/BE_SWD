package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.OrderRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.OrderResponse;
import com.officefood.healthy_food_api.mapper.OrderMapper;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final ServiceProvider sp;
    private final OrderMapper mapper;

    // GET /api/orders/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAll() {
        List<OrderResponse> orders = sp.orders()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Orders retrieved successfully", orders));
    }

    // GET /api/orders/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable String id) {
        return sp.orders()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(order -> ResponseEntity.ok(ApiResponse.success(200, "Order retrieved successfully", order)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Order not found")));
    }

    // POST /api/orders/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody OrderRequest req) {
        OrderResponse response = mapper.toResponse(sp.orders().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Order created successfully", response));
    }

    // PUT /api/orders/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> update(@PathVariable String id,
                              @Valid @RequestBody OrderRequest req) {
        Order entity = mapper.toEntity(req);
        entity.setId(id);
        OrderResponse response = mapper.toResponse(sp.orders().update(id, entity));
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
        OrderResponse response = mapper.toResponse(sp.orders().recalcTotals(id));
        return ResponseEntity.ok(ApiResponse.success(200, "Order totals recalculated successfully", response));
    }

    // POST /api/orders/apply-promo/{id}?code=...
    @PostMapping("/apply-promo/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> applyPromo(@PathVariable String id, @RequestParam String code) {
        OrderResponse response = mapper.toResponse(sp.orders().applyPromotion(id, code));
        return ResponseEntity.ok(ApiResponse.success(200, "Promotion applied successfully", response));
    }

    // POST /api/orders/confirm/{id}
    @PostMapping("/confirm/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> confirm(@PathVariable String id) {
        OrderResponse response = mapper.toResponse(sp.orders().confirm(id));
        return ResponseEntity.ok(ApiResponse.success(200, "Order confirmed successfully", response));
    }

    // POST /api/orders/cancel/{id}?reason=...
    @PostMapping("/cancel/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(@PathVariable String id, @RequestParam(required=false) String reason) {
        OrderResponse response = mapper.toResponse(sp.orders().cancel(id, reason));
        return ResponseEntity.ok(ApiResponse.success(200, "Order cancelled successfully", response));
    }

    // POST /api/orders/complete/{id}
    @PostMapping("/complete/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> complete(@PathVariable String id) {
        OrderResponse response = mapper.toResponse(sp.orders().complete(id));
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

        OrderResponse response = mapper.toResponse(updatedOrder);
        return ResponseEntity.ok(ApiResponse.success(200, "Order status updated successfully", response));
    }

    // GET /api/orders/user/{userId} - Get order history by user ID
    @GetMapping("/order-history/{userId}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getByUserId(@PathVariable String userId) {
        List<OrderResponse> orders = sp.orders()
                 .findByUserId(userId)
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Order history retrieved successfully", orders));
    }
}
