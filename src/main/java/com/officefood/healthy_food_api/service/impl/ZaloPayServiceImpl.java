package com.officefood.healthy_food_api.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.officefood.healthy_food_api.config.ZaloPayConfig;
import com.officefood.healthy_food_api.dto.request.ZaloPayCallbackRequest;
import com.officefood.healthy_food_api.dto.request.ZaloPayCreateOrderRequest;
import com.officefood.healthy_food_api.dto.response.ZaloPayCallbackResponse;
import com.officefood.healthy_food_api.dto.response.ZaloPayCreateOrderResponse;
import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.model.PaymentTransaction;
import com.officefood.healthy_food_api.model.enums.PaymentMethod;
import com.officefood.healthy_food_api.model.enums.PaymentStatus;
import com.officefood.healthy_food_api.repository.OrderRepository;
import com.officefood.healthy_food_api.repository.PaymentTransactionRepository;
import com.officefood.healthy_food_api.service.ZaloPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ZaloPayServiceImpl implements ZaloPayService {
    
    private final ZaloPayConfig zaloPayConfig;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    
    @Override
    public ZaloPayCreateOrderResponse createOrder(ZaloPayCreateOrderRequest request) throws Exception {
        // Find order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));
        
        // Create app_trans_id
        String currentDate = new SimpleDateFormat("yyMMdd").format(new Date());
        String appTransId = currentDate + "_" + System.currentTimeMillis();
        
        // Prepare order data
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("app_id", Integer.parseInt(zaloPayConfig.getAppId()));
        orderData.put("app_user", "user_" + order.getUser().getId());
        orderData.put("app_time", System.currentTimeMillis());
        orderData.put("app_trans_id", appTransId);
        orderData.put("amount", request.getAmount().longValue());
        orderData.put("description", request.getDescription() != null ? request.getDescription() : "Payment for order #" + order.getId());
        orderData.put("bank_code", request.getBankCode() != null ? request.getBankCode() : "");
        orderData.put("callback_url", zaloPayConfig.getCallbackUrl());
        orderData.put("redirect_url", zaloPayConfig.getRedirectUrl());
        
        // Embed order data
        Map<String, String> embedData = new HashMap<>();
        embedData.put("orderId", order.getId().toString());
        embedData.put("redirecturl", zaloPayConfig.getRedirectUrl());
        orderData.put("embed_data", objectMapper.writeValueAsString(embedData));
        
        // Create item array
        List<Map<String, String>> items = new ArrayList<>();
        Map<String, String> item = new HashMap<>();
        item.put("itemid", order.getId().toString());
        item.put("itemname", "Order #" + order.getId());
        item.put("itemprice", String.valueOf(request.getAmount().longValue()));
        item.put("itemquantity", "1");
        items.add(item);
        orderData.put("item", objectMapper.writeValueAsString(items));
        
        // Calculate MAC
        String data = orderData.get("app_id") + "|" + orderData.get("app_trans_id") + "|" 
                + orderData.get("app_user") + "|" + orderData.get("amount") + "|" 
                + orderData.get("app_time") + "|" + orderData.get("embed_data") + "|" 
                + orderData.get("item");
        String mac = hmacSHA256(data, zaloPayConfig.getKey1());
        orderData.put("mac", mac);
        
        // Send request to ZaloPay
        String response = sendPost(zaloPayConfig.getEndpoint(), orderData);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        
        // Create payment transaction
        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setOrder(order);
        paymentTransaction.setMethod(PaymentMethod.ZALOPAY);
        paymentTransaction.setStatus(PaymentStatus.PENDING);
        paymentTransaction.setAmount(request.getAmount());
        paymentTransaction.setProviderTxnId(appTransId);
        PaymentTransaction savedPayment = paymentTransactionRepository.save(paymentTransaction);

        log.info("Created ZaloPay order: appTransId={}, paymentTransactionId={}, amount={}, response={}",
                appTransId, savedPayment.getId(), request.getAmount(), response);

        // Build response
        return ZaloPayCreateOrderResponse.builder()
                .returnCode((Integer) responseMap.get("return_code"))
                .returnMessage((String) responseMap.get("return_message"))
                .orderUrl((String) responseMap.get("order_url"))
                .zpTransToken((String) responseMap.get("zp_trans_token"))
                .appTransId(appTransId)
                .paymentTransactionId(savedPayment.getId())
                .amount(request.getAmount())
                .build();
    }
    
    @Override
    public ZaloPayCallbackResponse handleCallback(ZaloPayCallbackRequest request) throws Exception {
        log.info("=== RECEIVED ZALOPAY CALLBACK ===");
        log.info("Callback data: {}", request.getData());
        log.info("Callback MAC: {}", request.getMac());

        try {
            // Verify MAC
            String reqMac = request.getMac();
            String calculatedMac = hmacSHA256(request.getData(), zaloPayConfig.getKey2());
            
            log.info("Calculated MAC: {}", calculatedMac);
            log.info("MAC verification: {}", reqMac.equals(calculatedMac) ? "PASSED" : "FAILED");

            if (!reqMac.equals(calculatedMac)) {
                log.error("Invalid callback MAC - Expected: {}, Received: {}", calculatedMac, reqMac);
                return ZaloPayCallbackResponse.builder()
                        .returnCode(-1)
                        .returnMessage("mac not equal")
                        .build();
            }
            
            // Parse callback data
            @SuppressWarnings("unchecked")
            Map<String, Object> callbackData = objectMapper.readValue(request.getData(), Map.class);
            String appTransId = (String) callbackData.get("app_trans_id");
            Integer zpStatus = (Integer) callbackData.get("status");

            log.info("Parsed callback - appTransId: {}, zpStatus: {}", appTransId, zpStatus);

            // Find payment transaction
            PaymentTransaction payment = paymentTransactionRepository.findByProviderTxnId(appTransId)
                    .orElseThrow(() -> {
                        log.error("Payment transaction not found for appTransId: {}", appTransId);
                        return new NotFoundException("Payment transaction not found");
                    });

            log.info("Found payment transaction - ID: {}, Current Status: {}", payment.getId(), payment.getStatus());

            // Update payment status based on ZaloPay status
            if (zpStatus == 1) {
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setCapturedAt(OffsetDateTime.now());
                log.info("Updated payment status to CAPTURED");
            } else {
                payment.setStatus(PaymentStatus.FAILED);
                log.info("Updated payment status to FAILED");
            }
            
            paymentTransactionRepository.save(payment);
            log.info("Payment transaction saved successfully");

            log.info("=== ZALOPAY CALLBACK PROCESSED SUCCESSFULLY ===");

            return ZaloPayCallbackResponse.builder()
                    .returnCode(1)
                    .returnMessage("success")
                    .build();
                    
        } catch (Exception e) {
            log.error("=== ERROR PROCESSING ZALOPAY CALLBACK ===", e);
            log.error("Error message: {}", e.getMessage());
            log.error("Error class: {}", e.getClass().getName());
            return ZaloPayCallbackResponse.builder()
                    .returnCode(0)
                    .returnMessage("error: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public String queryPaymentStatus(String appTransId) throws Exception {
        String endpoint = "https://sb-openapi.zalopay.vn/v2/query";
        
        Map<String, Object> queryData = new HashMap<>();
        queryData.put("app_id", Integer.parseInt(zaloPayConfig.getAppId()));
        queryData.put("app_trans_id", appTransId);
        
        String data = queryData.get("app_id") + "|" + queryData.get("app_trans_id") + "|" + zaloPayConfig.getKey1();
        String mac = hmacSHA256(data, zaloPayConfig.getKey1());
        queryData.put("mac", mac);
        
        return sendPost(endpoint, queryData);
    }
    
    /**
     * Update payment status by payment transaction ID
     * @param paymentTransactionId Payment transaction ID
     * @param forceStatus Optional: 1 = force SUCCESS, 2 = force FAIL, null = query ZaloPay
     */
    @Override
    public void updatePaymentStatus(String paymentTransactionId, Integer forceStatus) throws Exception {
        log.info("Updating payment status for payment transaction ID: {}, forceStatus: {}",
                paymentTransactionId, forceStatus);

        // Find payment transaction
        PaymentTransaction payment = paymentTransactionRepository.findById(paymentTransactionId)
                .orElseThrow(() -> new NotFoundException("Payment transaction not found"));

        log.info("Current payment status: {}", payment.getStatus());

        if (forceStatus != null) {
            // Force update without querying ZaloPay
            if (forceStatus == 1) {
                // Force SUCCESS
                log.info("Force updating to SUCCESS (CAPTURED)");
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setCapturedAt(OffsetDateTime.now());
            } else if (forceStatus == 2) {
                // Force FAILED
                log.info("Force updating to FAILED");
                payment.setStatus(PaymentStatus.FAILED);
            } else {
                throw new IllegalArgumentException("Invalid forceStatus. Use 1 for SUCCESS or 2 for FAIL");
            }
        } else {
            // Query ZaloPay and update based on response
            String appTransId = payment.getProviderTxnId();

            if (appTransId == null || appTransId.isEmpty()) {
                throw new IllegalStateException("Payment transaction does not have provider transaction ID");
            }

            log.info("Querying ZaloPay for appTransId: {}", appTransId);

            String queryResponse = queryPaymentStatus(appTransId);
            log.info("ZaloPay query response: {}", queryResponse);

            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(queryResponse, Map.class);
            Integer returnCode = (Integer) responseMap.get("return_code");

            // return_code = 1: Payment successful
            // return_code = 2: Payment failed
            // return_code = 3: Payment pending
            if (returnCode == 1) {
                payment.setStatus(PaymentStatus.CAPTURED);
                payment.setCapturedAt(OffsetDateTime.now());
                log.info("Updated payment status to CAPTURED based on ZaloPay");
            } else if (returnCode == 2) {
                payment.setStatus(PaymentStatus.FAILED);
                log.info("Updated payment status to FAILED based on ZaloPay");
            } else {
                log.info("Payment still PENDING on ZaloPay, no update");
                return; // Don't save if still pending
            }
        }

        paymentTransactionRepository.save(payment);
        log.info("Payment transaction updated successfully to: {}", payment.getStatus());
    }

    @Override
    public String refundPayment(String paymentTransactionId, Double amount, String description) throws Exception {
        PaymentTransaction payment = paymentTransactionRepository.findById(paymentTransactionId)
                .orElseThrow(() -> new NotFoundException("Payment transaction not found"));
        
        String endpoint = "https://sb-openapi.zalopay.vn/v2/refund";
        
        String mRefundId = new SimpleDateFormat("yyMMdd").format(new Date()) + "_" + payment.getId() + "_" + System.currentTimeMillis();
        long timestamp = System.currentTimeMillis();
        
        Map<String, Object> refundData = new HashMap<>();
        refundData.put("app_id", Integer.parseInt(zaloPayConfig.getAppId()));
        refundData.put("zp_trans_id", payment.getProviderTxnId());
        refundData.put("amount", amount.longValue());
        refundData.put("timestamp", timestamp);
        refundData.put("description", description != null ? description : "Refund for order");
        refundData.put("m_refund_id", mRefundId);
        
        String data = refundData.get("app_id") + "|" + refundData.get("zp_trans_id") + "|" 
                + refundData.get("amount") + "|" + refundData.get("description") + "|" + timestamp;
        String mac = hmacSHA256(data, zaloPayConfig.getKey1());
        refundData.put("mac", mac);
        
        String response = sendPost(endpoint, refundData);
        
        // Update payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentTransactionRepository.save(payment);
        
        return response;
    }
    
    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private String sendPost(String urlString, Map<String, Object> params) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (!postData.isEmpty()) postData.append('&');
            postData.append(param.getKey());
            postData.append('=');
            postData.append(java.net.URLEncoder.encode(String.valueOf(param.getValue()), StandardCharsets.UTF_8));
        }
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(postData.toString().getBytes(StandardCharsets.UTF_8));
        }
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        return response.toString();
    }
}

