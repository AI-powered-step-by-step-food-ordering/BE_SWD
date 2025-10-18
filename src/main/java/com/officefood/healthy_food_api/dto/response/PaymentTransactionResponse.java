package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class PaymentTransactionResponse {
    private java.util.UUID id;
    private java.util.UUID orderId;
    private String method;
    private String status;
    private Double amount;
}
