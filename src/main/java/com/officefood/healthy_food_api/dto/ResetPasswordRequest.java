package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "ResetPasswordRequest", description = "Yêu cầu đặt lại mật khẩu bằng OTP")
public record ResetPasswordRequest(
        @Schema(example = "user@example.com")
        @NotBlank @Email String email,

        @Schema(example = "123456")
        @NotBlank
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải là 6 chữ số")
        String otp,

        @Schema(example = "P@ssw0rd!")
        @NotBlank @Size(min = 6, max = 100)
        String newPassword,

        @Schema(example = "P@ssw0rd!")
        @NotBlank @Size(min = 6, max = 100)
        String passwordConfirm
) {}


