package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class OrderResponse {
    private java.util.UUID id;
    private java.util.UUID userId;
    private java.util.UUID storeId;
    private String status;
    private Double subtotalAmount;
    private Double promotionTotal;
    private Double totalAmount;
}
