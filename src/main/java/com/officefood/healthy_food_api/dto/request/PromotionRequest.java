package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class PromotionRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    @NotBlank private String type;
    private Double percentOff;
    private Double amountOff;
    private Double minOrderValue;
    private java.time.OffsetDateTime startsAt;
    private java.time.OffsetDateTime endsAt;
    private Integer maxRedemptions;
    private Integer perOrderLimit;
    private Boolean isActive;
}
