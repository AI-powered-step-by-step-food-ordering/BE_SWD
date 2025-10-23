package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmailVerificationResponse", description = "Kết quả xác thực email")
public record EmailVerificationResponse(
        @Schema(example = "user@example.com") String email,
        @Schema(example = "Nguyen Van A") String fullName,
        @Schema(example = "PENDING_VERIFICATION") String status,
        @Schema(example = "false") Boolean emailVerified,
        @Schema(example = "Email verification sent successfully") String message
) {}
