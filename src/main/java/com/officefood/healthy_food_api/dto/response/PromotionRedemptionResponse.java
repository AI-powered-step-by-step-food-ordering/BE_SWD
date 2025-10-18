package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class PromotionRedemptionResponse {
    private java.util.UUID id;
    private java.util.UUID promotionId;
    private java.util.UUID orderId;
    private String status;
}
