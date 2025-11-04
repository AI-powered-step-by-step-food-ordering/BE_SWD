package com.officefood.healthy_food_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "GoogleLoginRequest", description = "Google ID token for third-party login")
public record GoogleLoginRequest(
        @NotBlank
        @Schema(description = "Google ID token from Google Sign-In") String idToken
) {}


