package com.officefood.healthy_food_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZaloPayCreateOrderResponse {
    private Integer returnCode;
    private String returnMessage;
    private String orderUrl;
    private String zpTransToken;
    private String appTransId;
    private String paymentTransactionId;  // ID của PaymentTransaction trong database
    private Double amount;  // Số tiền thanh toán
}

