package com.officefood.healthy_food_api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Size;

public class UserRequest {

    @Schema(name = "UserCreate")
    public record Create(
            @Schema(example = "Nguyen Van A")  @NotBlank @Size(max = 255) String fullName,
            @Schema(example = "user@example.com")  @NotBlank @Email @Size(max = 255) String email,
            @Schema(example = "ACME Co") @Size(max = 255) String companyName,
            @Schema(example = "LOSE_WEIGHT") @Size(max = 50) String goalCode,
            @Schema(example = "P@ssw0rd!") @NotBlank @Size(min = 6, max = 100) String password,
            @Schema(example = "P@ssw0rd!") String passwordConfirm
    ) {}

    @Schema(name = "UserUpdate")
    public record Update(
            @Schema(example = "Nguyen Van A") @NotBlank @Size(max = 255) String fullName,
            @Schema(example = "ACME Co") @Size(max = 255) String companyName,
            @Schema(example = "LOSE_WEIGHT") @Size(max = 50) String goalCode
    ) {}
}


