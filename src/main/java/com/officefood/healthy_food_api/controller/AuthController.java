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
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> register(@Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest req) {
        var result = sp.auth().register(req);
        return ResponseEntity.ok(ApiResponse.success(200, "Registered successfully. OTP sent to your email", result));
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

    @PostMapping(value = "/verify-otp", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Xác thực email bằng OTP", description = "Xác thực email bằng mã OTP 6 chữ số")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyOtp(
            @Valid @org.springframework.web.bind.annotation.RequestBody VerifyOtpRequest request) {
        var result = sp.auth().verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Email verified successfully", result));
    }

    @PostMapping("/resend-verification-otp")
    @Operation(summary = "Gửi lại mã OTP xác thực", description = "Gửi lại OTP xác thực email cho người dùng")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> resendVerificationOtp(@RequestParam String email) {
        var result = sp.auth().resendVerificationOtp(email);
        return ResponseEntity.ok(ApiResponse.success(200, "Verification OTP sent successfully", result));
    }

    @PostMapping(value = "/forgot-password", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Quên mật khẩu", description = "Gửi OTP đặt lại mật khẩu tới email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @org.springframework.web.bind.annotation.RequestBody ForgotPasswordRequest request) {
        sp.auth().forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Password reset OTP sent successfully", null));
    }

    @PostMapping(value = "/reset-password", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt lại mật khẩu bằng OTP 6 chữ số")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @org.springframework.web.bind.annotation.RequestBody ResetPasswordRequest request) {
        sp.auth().resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(200, "Password reset successfully", null));
    }
}
