package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.*;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.mapper.AuthMapper;
import com.officefood.healthy_food_api.provider.ServiceProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name="Auth")
@Validated
@RequiredArgsConstructor
public class AuthController {
    private final ServiceProvider sp;
    private final AuthMapper mapper;

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Đăng ký", requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = RegisterRequest.class))))
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest req) {
        var result = sp.auth().register(req);
        return ResponseEntity.ok(ApiResponse.success(200, "Registered successfully", result));
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Đăng nhập", requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = LoginRequest.class))))
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest req) {
        var result = sp.auth().login(req);
        return ResponseEntity.ok(ApiResponse.success(200, "Login successful", result));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String bearer) {
        sp.auth().logout(bearer);
        return ResponseEntity.ok(ApiResponse.success(200, "Logged out", null));
    }

    @PostMapping(value = "/refresh", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Làm mới token", requestBody = @RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))))
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(@Valid @org.springframework.web.bind.annotation.RequestBody RefreshTokenRequest req) {
        var result = sp.auth().refreshToken(req);
        return ResponseEntity.ok(ApiResponse.success(200, "Token refreshed successfully", result));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Xác thực email", description = "Xác thực email bằng token")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(@RequestParam String token) {
        var result = sp.auth().verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(200, "Email verified successfully", result));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Gửi lại email xác thực", description = "Gửi lại email xác thực cho người dùng")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> resendVerificationEmail(@RequestParam String email) {
        var result = sp.auth().resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success(200, "Verification email sent successfully", result));
    }
}
