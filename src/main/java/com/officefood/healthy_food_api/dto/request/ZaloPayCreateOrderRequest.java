package com.officefood.healthy_food_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ZaloPayCreateOrderRequest {
    @NotNull(message = "Order ID is required")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    private String description;

    private String bankCode; // Optional: specific bank code for direct payment
}

