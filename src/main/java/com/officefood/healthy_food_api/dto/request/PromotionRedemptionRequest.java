package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class PromotionRedemptionRequest {
    private String status;
    @NotNull private String promotionId;
    @NotNull private String orderId;
}
