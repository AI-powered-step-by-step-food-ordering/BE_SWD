package com.officefood.healthy_food_api.controller;

import com.officefood.healthy_food_api.dto.request.FcmTokenRequest;
import com.officefood.healthy_food_api.dto.request.PromotionalNotificationRequest;
import com.officefood.healthy_food_api.dto.response.ApiResponse;
import com.officefood.healthy_food_api.dto.response.NotificationResponse;
import com.officefood.healthy_food_api.service.FcmService;
import com.officefood.healthy_food_api.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Push notification and notification history management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final FcmService fcmService;
    private final NotificationService notificationService;

    // ======================== FCM Token Management ========================

    @PutMapping("/users/{userId}/fcm-token")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Save or update FCM token for push notifications")
    public ResponseEntity<ApiResponse> saveFcmToken(
            @PathVariable UUID userId,
            @Valid @RequestBody FcmTokenRequest request) {
        try {
            fcmService.updateFcmToken(userId, request.getFcmToken(),
                    request.getPlatform(), request.getDeviceId());

            return ResponseEntity.ok(ApiResponse.success("FCM token saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save FCM token: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}/fcm-token")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Remove FCM token (on logout)")
    public ResponseEntity<ApiResponse> removeFcmToken(@PathVariable UUID userId) {
        try {
            fcmService.removeFcmToken(userId);
            return ResponseEntity.ok(ApiResponse.success("FCM token removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to remove FCM token: " + e.getMessage()));
        }
    }

    // ======================== Notification History ========================

    @GetMapping("/users/{userId}/notifications")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Get user's notification history")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/users/{userId}/notifications/unread")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(@PathVariable UUID userId) {
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/users/{userId}/notifications/unread-count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/notifications/{notificationId}/read")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable UUID notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to mark notification as read: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/notifications/read-all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'KITCHEN_STAFF')")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponse> markAllAsRead(@PathVariable UUID userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to mark all as read: " + e.getMessage()));
        }
    }

    // ======================== Promotional Notifications (Admin Only) ========================

    @PostMapping("/notifications/promotion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send promotional notification to multiple users (Admin only)")
    public ResponseEntity<ApiResponse> sendPromotionalNotification(
            @Valid @RequestBody PromotionalNotificationRequest request) {
        try {
            List<UUID> userIds = request.getUserIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());

            Map<String, Object> result = fcmService.sendPromotionalNotification(
                    userIds,
                    request.getTitle(),
                    request.getMessage(),
                    request.getImageUrl(),
                    request.getPromoCode()
            );

            return ResponseEntity.ok(ApiResponse.success("Promotional notifications sent", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to send promotional notifications: " + e.getMessage()));
        }
    }
}

