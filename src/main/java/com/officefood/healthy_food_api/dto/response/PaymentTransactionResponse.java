package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class PaymentTransactionResponse {
    private String id;
    private String orderId;
    private String method;
    private String status;
    private Double amount;
}
