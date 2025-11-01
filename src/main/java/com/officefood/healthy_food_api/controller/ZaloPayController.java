package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.ZaloPayCallbackRequest;
import com.officefood.healthy_food_api.dto.request.ZaloPayCreateOrderRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.ZaloPayCallbackResponse;
import com.officefood.healthy_food_api.dto.response.ZaloPayCreateOrderResponse;
import com.officefood.healthy_food_api.service.ZaloPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/zalopay")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ZaloPay", description = "ZaloPay payment integration endpoints")
public class ZaloPayController {

    private final ZaloPayService zaloPayService;

    @PostMapping("/create-order")
    @Operation(summary = "Create ZaloPay payment order", description = "Create a new payment order with ZaloPay")
    public ResponseEntity<ApiResponse<ZaloPayCreateOrderResponse>> createOrder(
            @Valid @RequestBody ZaloPayCreateOrderRequest request) {
        try {
            ZaloPayCreateOrderResponse response = zaloPayService.createOrder(request);

            if (response.getReturnCode() == 1) {
                return ResponseEntity.ok(ApiResponse.success(200, "Order created successfully", response));
            } else {
                return ResponseEntity.ok(ApiResponse.error(400, "CREATE_ORDER_FAILED", response.getReturnMessage()));
            }
        } catch (Exception e) {
            log.error("Error creating ZaloPay order", e);
            return ResponseEntity.ok(ApiResponse.error(500, "INTERNAL_ERROR", "Failed to create order: " + e.getMessage()));
        }
    }

    @PostMapping("/callback")
    @Operation(summary = "ZaloPay callback", description = "Handle payment callback from ZaloPay")
    public ResponseEntity<ZaloPayCallbackResponse> handleCallback(@RequestBody ZaloPayCallbackRequest request) {
        try {
            ZaloPayCallbackResponse response = zaloPayService.handleCallback(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error handling ZaloPay callback", e);
            return ResponseEntity.ok(ZaloPayCallbackResponse.builder()
                    .returnCode(0)
                    .returnMessage("error: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/query/{appTransId}")
    @Operation(summary = "Query payment status", description = "Query the status of a ZaloPay payment")
    public ResponseEntity<ApiResponse<String>> queryPaymentStatus(@PathVariable String appTransId) {
        try {
            String response = zaloPayService.queryPaymentStatus(appTransId);
            return ResponseEntity.ok(ApiResponse.success(200, "Query successful", response));
        } catch (Exception e) {
            log.error("Error querying ZaloPay payment status", e);
            return ResponseEntity.ok(ApiResponse.error(500, "QUERY_FAILED", "Failed to query payment: " + e.getMessage()));
        }
    }

    @PostMapping("/update-status/{paymentTransactionId}")
    @Operation(
        summary = "Update payment status",
        description = "Automatically query ZaloPay and update payment status. " +
                      "Default behavior: Query ZaloPay to get real payment status. " +
                      "Optional forceStatus parameter: 1 = force SUCCESS (CAPTURED), 2 = force FAIL"
    )
    public ResponseEntity<ApiResponse<String>> updatePaymentStatus(
            @PathVariable String paymentTransactionId,
            @RequestParam(required = false) Integer forceStatus) {
        try {
            log.info("Update payment status request - paymentId: {}, forceStatus: {}",
                    paymentTransactionId, forceStatus);

            zaloPayService.updatePaymentStatus(paymentTransactionId, forceStatus);

            String message;
            if (forceStatus == null) {
                message = "Payment status synced from ZaloPay successfully";
            } else if (forceStatus == 1) {
                message = "Payment status manually set to SUCCESS (CAPTURED)";
            } else if (forceStatus == 2) {
                message = "Payment status manually set to FAILED";
            } else {
                message = "Payment status updated";
            }

            log.info("Payment status update completed - paymentId: {}, result: {}",
                    paymentTransactionId, message);

            return ResponseEntity.ok(ApiResponse.success(200, message, paymentTransactionId.toString()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid forceStatus parameter: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(400, "INVALID_PARAMETER",
                    "Invalid forceStatus value. Use 1 for SUCCESS or 2 for FAIL"));
        } catch (Exception e) {
            log.error("Error updating payment status for paymentId: {}", paymentTransactionId, e);
            return ResponseEntity.ok(ApiResponse.error(500, "UPDATE_FAILED",
                    "Failed to update payment status: " + e.getMessage()));
        }
    }

    @PostMapping("/refund/{paymentTransactionId}")
    @Operation(summary = "Refund payment", description = "Refund a ZaloPay payment")
    public ResponseEntity<ApiResponse<String>> refundPayment(
            @PathVariable String paymentTransactionId,
            @RequestParam Double amount,
            @RequestParam(required = false) String description) {
        try {
            String response = zaloPayService.refundPayment(paymentTransactionId, amount, description);
            return ResponseEntity.ok(ApiResponse.success(200, "Refund initiated", response));
        } catch (Exception e) {
            log.error("Error refunding ZaloPay payment", e);
            return ResponseEntity.ok(ApiResponse.error(500, "REFUND_FAILED", "Failed to refund payment: " + e.getMessage()));
        }
    }
}

