package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class PaymentTransactionRequest {
    @NotBlank private String method;
    @NotBlank private String status;
    @NotNull private Double amount;
    private String providerTxnId;
    @NotNull private String orderId;
}
