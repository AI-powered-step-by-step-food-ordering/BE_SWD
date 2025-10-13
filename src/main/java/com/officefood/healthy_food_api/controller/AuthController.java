package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.*;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name="Auth")
@Validated
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Đăng ký", requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = RegisterRequest.class))))
    public ResponseEntity<ApiResponse<AuthResponse>> register(@org.springframework.web.bind.annotation.RequestBody RegisterRequest req) {
        var result = auth.register(req);
        return ResponseEntity.ok(ApiResponse.success(200, "Registered successfully", result));
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Đăng nhập", requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = LoginRequest.class))))
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest req) {
        var result = auth.login(req);
        return ResponseEntity.ok(ApiResponse.success(200, "Login successful", result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String bearer) {
        auth.logout(bearer);
        return ResponseEntity.ok(ApiResponse.success(200, "Logged out", null));
    }
}
