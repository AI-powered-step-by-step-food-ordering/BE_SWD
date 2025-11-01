package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.PaymentTransactionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PaymentTransactionResponse;
import com.officefood.healthy_food_api.mapper.PaymentTransactionMapper;
import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment_transactions")
@RequiredArgsConstructor
public class PaymentTransactionController {
    private final ServiceProvider sp;
    private final PaymentTransactionMapper mapper;

    // GET /api/payment_transactions/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> getAll() {
        List<PaymentTransactionResponse> paymentTransactions = sp.payments()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Payment transactions retrieved successfully", paymentTransactions));
    }

    // GET /api/payment_transactions/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> getById(@PathVariable String id) {
        return sp.payments()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(payment -> ResponseEntity.ok(ApiResponse.success(200, "Payment transaction retrieved successfully", payment)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Payment transaction not found")));
    }

    // POST /api/payment_transactions/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> create(@Valid @RequestBody PaymentTransactionRequest req) {
        PaymentTransactionResponse response = mapper.toResponse(sp.payments().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Payment transaction created successfully", response));
    }

    // PUT /api/payment_transactions/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> update(@PathVariable String id,
                              @Valid @RequestBody PaymentTransactionRequest req) {
        PaymentTransaction entity = mapper.toEntity(req);
        entity.setId(id);
        PaymentTransactionResponse response = mapper.toResponse(sp.payments().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Payment transaction updated successfully", response));
    }

    // DELETE /api/payment_transactions/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.payments().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Payment transaction deleted successfully", null));
    }

    // GET /api/payment_transactions/user/{userId} - Get payment history by user ID
    @GetMapping("/payment-history/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> getByUserId(@PathVariable String userId) {
        List<PaymentTransactionResponse> transactions = sp.payments()
                 .findByUserId(userId)
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Payment transaction history retrieved successfully", transactions));
    }
}
