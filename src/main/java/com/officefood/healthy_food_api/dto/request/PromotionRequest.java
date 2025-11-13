package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class PromotionRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double discountPercent;
    private java.time.OffsetDateTime startsAt;
    private java.time.OffsetDateTime endsAt;
    private Boolean isActive;
    private String imageUrl;
}
