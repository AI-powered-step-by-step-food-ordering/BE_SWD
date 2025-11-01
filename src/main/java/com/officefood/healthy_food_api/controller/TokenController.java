package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.TokenRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.TokenResponse;
import com.officefood.healthy_food_api.mapper.TokenMapper;
import com.officefood.healthy_food_api.model.Token;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {
    private final ServiceProvider sp;
    private final TokenMapper mapper;

    // GET /api/tokens/getall
    @GetMapping("/getall")
    public ResponseEntity<ApiResponse<List<TokenResponse>>> getAll() {
        List<TokenResponse> tokens = sp.tokens()
                 .findAll()
                 .stream()
                 .map(mapper::toResponse)
                 .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(200, "Tokens retrieved successfully", tokens));
    }

    // GET /api/tokens/getbyid/{id}
    @GetMapping("/getbyid/{id}")
    public ResponseEntity<ApiResponse<TokenResponse>> getById(@PathVariable String id) {
        return sp.tokens()
                 .findById(id)
                 .map(mapper::toResponse)
                 .map(token -> ResponseEntity.ok(ApiResponse.success(200, "Token retrieved successfully", token)))
                 .orElse(ResponseEntity.ok(ApiResponse.error(404, "NOT_FOUND", "Token not found")));
    }

    // POST /api/tokens/create
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TokenResponse>> create(@Valid @RequestBody TokenRequest req) {
        TokenResponse response = mapper.toResponse(sp.tokens().create(mapper.toEntity(req)));
        return ResponseEntity.ok(ApiResponse.success(201, "Token created successfully", response));
    }

    // PUT /api/tokens/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<TokenResponse>> update(@PathVariable String id,
                              @Valid @RequestBody TokenRequest req) {
        Token entity = mapper.toEntity(req);
        entity.setId(id);
        TokenResponse response = mapper.toResponse(sp.tokens().update(id, entity));
        return ResponseEntity.ok(ApiResponse.success(200, "Token updated successfully", response));
    }

    // DELETE /api/tokens/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        sp.tokens().deleteById(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Token deleted successfully", null));
    }
}
