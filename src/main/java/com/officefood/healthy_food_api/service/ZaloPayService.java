package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.request.ZaloPayCallbackRequest;
import com.officefood.healthy_food_api.dto.request.ZaloPayCreateOrderRequest;
import com.officefood.healthy_food_api.dto.response.ZaloPayCallbackResponse;
import com.officefood.healthy_food_api.dto.response.ZaloPayCreateOrderResponse;

import java.util.UUID;

public interface ZaloPayService {
    /**
     * Create a ZaloPay payment order
     */
    ZaloPayCreateOrderResponse createOrder(ZaloPayCreateOrderRequest request) throws Exception;

    /**
     * Handle ZaloPay callback when payment is completed
     */
    ZaloPayCallbackResponse handleCallback(ZaloPayCallbackRequest request) throws Exception;

    /**
     * Query payment status
     */
    String queryPaymentStatus(String appTransId) throws Exception;

    /**
     * Update payment status by payment transaction ID
     * @param paymentTransactionId Payment transaction ID
     * @param forceStatus Optional: 1 = force SUCCESS, 2 = force FAIL, null = query ZaloPay
     */
    void updatePaymentStatus(UUID paymentTransactionId, Integer forceStatus) throws Exception;

    /**
     * Refund a payment
     */
    String refundPayment(UUID paymentTransactionId, Double amount, String description) throws Exception;
}

