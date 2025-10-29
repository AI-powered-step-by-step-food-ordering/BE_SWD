package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ForgotPasswordRequest", description = "Yêu cầu quên mật khẩu - gửi OTP đặt lại")
public record ForgotPasswordRequest(
        @Schema(example = "user@example.com")
        @NotBlank @Email String email
) {}


