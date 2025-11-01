package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class PromotionRedemptionResponse {
    private String id;
    private String promotionId;
    private String orderId;
    private String status;
}
