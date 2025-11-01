package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class OrderResponse {
    private String id;
    private String userId;
    private String storeId;
    private String status;
    private Double subtotalAmount;
    private Double promotionTotal;
    private Double totalAmount;
    private ZonedDateTime createdAt;
}
