package com.officefood.healthy_food_api.controller;
import com.officefood.healthy_food_api.dto.request.PaymentTransactionRequest;
import com.officefood.healthy_food_api.dto.response.PaymentTransactionResponse;
import com.officefood.healthy_food_api.mapper.PaymentTransactionMapper;
import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.*; import java.util.stream.Collectors;

@RestController @RequestMapping("/api/paymentTransactions") @RequiredArgsConstructor
public class PaymentTransactionController {
    private final PaymentTransactionService service;
    private final PaymentTransactionMapper mapper;

    @GetMapping("/getall") public List<PaymentTransactionResponse> list() {
        return service.findAll().stream().map(mapper::toResponse).collect(Collectors.toList());
    }
    @GetMapping("/getbyid") public ResponseEntity<PaymentTransactionResponse> get(@PathVariable java.util.UUID id) {
        return service.findById(id).map(mapper::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/create") public PaymentTransactionResponse create(@Valid @RequestBody PaymentTransactionRequest req) {
        return mapper.toResponse(service.create(mapper.toEntity(req)));
    }
    @PutMapping("/update") public PaymentTransactionResponse update(@PathVariable java.util.UUID id, @Valid @RequestBody PaymentTransactionRequest req) {
        PaymentTransaction e = mapper.toEntity(req); e.setId(id); return mapper.toResponse(service.update(id, e));
    }
    @DeleteMapping("/delete") public ResponseEntity<Void> delete(@PathVariable java.util.UUID id) {
        service.deleteById(id); return ResponseEntity.noContent().build();
    }

}
