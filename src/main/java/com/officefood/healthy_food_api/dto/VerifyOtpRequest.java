package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "VerifyOtpRequest", description = "Request để xác thực email bằng OTP")
public record VerifyOtpRequest(
        @Schema(example = "user@example.com", description = "Email của người dùng")
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @Schema(example = "123456", description = "Mã OTP 6 chữ số")
        @NotBlank(message = "OTP không được để trống")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP phải là 6 chữ số")
        @Size(min = 6, max = 6, message = "OTP phải có đúng 6 chữ số")
        String otp
) {}
