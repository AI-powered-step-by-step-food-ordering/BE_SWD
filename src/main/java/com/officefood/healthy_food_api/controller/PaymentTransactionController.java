package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.PaymentTransactionRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.PagedResponse;
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
    public ResponseEntity<ApiResponse<PagedResponse<PaymentTransactionResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<PaymentTransaction> allTransactions = sp.payments().findAll();
        List<PaymentTransaction> sortedTransactions = com.officefood.healthy_food_api.utils.SortUtils.sortEntities(allTransactions, sortBy, sortDir);
        PagedResponse<PaymentTransactionResponse> pagedResponse = createPagedResponse(sortedTransactions, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Payment transactions retrieved successfully", pagedResponse));
    }

    /**
     * Helper method to create PagedResponse from PaymentTransaction list
     */
    private PagedResponse<PaymentTransactionResponse> createPagedResponse(List<PaymentTransaction> transactions, int page, int size) {
        if (size < 1) size = 5;
        if (page < 0) page = 0;

        int totalElements = transactions.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // Nếu page vượt quá totalPages, trả về empty list
        List<PaymentTransactionResponse> pageContent;
        if (page >= totalPages && totalPages > 0) {
            pageContent = List.of();
        } else {
            int startIndex = page * size;
            pageContent = transactions.stream()
                    .skip(startIndex)
                    .limit(size)
                    .map(mapper::toResponse)
                    .collect(Collectors.toList());
        }

        return PagedResponse.<PaymentTransactionResponse>builder()
                .content(pageContent)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
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

    // GET /api/payment_transactions/payment-history/{userId} - Get payment history by user ID with pagination and sorting
    @GetMapping("/payment-history/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentTransactionResponse>>> getByUserId(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        List<PaymentTransaction> userTransactions = sp.payments().findByUserId(userId);
        List<PaymentTransaction> sortedTransactions = com.officefood.healthy_food_api.utils.SortUtils.sortEntities(userTransactions, sortBy, sortDir);
        PagedResponse<PaymentTransactionResponse> pagedResponse = createPagedResponse(sortedTransactions, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Payment transaction history retrieved successfully", pagedResponse));
    }
}
