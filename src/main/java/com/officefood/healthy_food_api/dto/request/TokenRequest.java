package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class TokenRequest {
    @NotBlank private String accessToken;
    private String refreshToken;
    private java.time.OffsetDateTime expiresAt;
    @NotNull private java.util.UUID userId;
}
