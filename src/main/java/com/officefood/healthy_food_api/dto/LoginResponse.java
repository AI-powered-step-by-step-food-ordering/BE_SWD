package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Kết quả đăng nhập/đăng ký")
public record LoginResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken,
        @Schema(example = "Bearer") String tokenType,
        @Schema(example = "user@example.com") String email,
        @Schema(example = "Nguyen Van A") String fullName,
        @Schema(example = "86400") long expiresIn
) {}
