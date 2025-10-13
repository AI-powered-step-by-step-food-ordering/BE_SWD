package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "Thông tin đăng nhập")
public record LoginRequest(
        @Schema(example = "user@example.com") @Email String email,
        @Schema(example = "P@ssw0rd!") @NotBlank String password
) {
}
