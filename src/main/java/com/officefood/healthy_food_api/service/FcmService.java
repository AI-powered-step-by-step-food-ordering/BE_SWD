package com.officefood.healthy_food_api.service;

import com.google.firebase.messaging.*;
import com.officefood.healthy_food_api.model.Notification;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.model.enums.NotificationType;
import com.officefood.healthy_food_api.model.enums.OrderStatus;
import com.officefood.healthy_food_api.repository.NotificationRepository;
import com.officefood.healthy_food_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Send notification for order status update
     */
    @Transactional
    public void sendOrderNotification(Order order, OrderStatus status) {
        User user = order.getUser();
        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            log.warn("User {} has no FCM token. Skipping notification.", user.getId());
            return;
        }

        Map<String, String> messageContent = getOrderStatusMessage(status, order);
        String title = messageContent.get("title");
        String body = messageContent.get("body");

        sendNotification(user, title, body, NotificationType.ORDER_UPDATE, order, status);
    }

    /**
     * Send promotional notification to multiple users
     */
    @Transactional
    public Map<String, Object> sendPromotionalNotification(List<String> userIds, String title, String message,
                                                           String imageUrl, String promoCode) {
        int successCount = 0;
        int failCount = 0;
        List<String> failedUserIds = new ArrayList<>();

        for (String userId : userIds) {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || userOpt.get().getFcmToken() == null) {
                failCount++;
                failedUserIds.add(userId.toString());
                continue;
            }

            User user = userOpt.get();
            Map<String, String> data = new HashMap<>();
            if (promoCode != null) {
                data.put("promoCode", promoCode);
            }

            boolean sent = sendNotification(user, title, message, NotificationType.PROMOTION, null, null, data, imageUrl);
            if (sent) {
                successCount++;
            } else {
                failCount++;
                failedUserIds.add(userId.toString());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", userIds.size());
        result.put("success", successCount);
        result.put("failed", failCount);
        result.put("failedUserIds", failedUserIds);

        return result;
    }

    /**
     * Core method to send FCM notification
     */
    private boolean sendNotification(User user, String title, String body,
                                     NotificationType type, Order order, OrderStatus orderStatus) {
        return sendNotification(user, title, body, type, order, orderStatus, new HashMap<>(), null);
    }

    private boolean sendNotification(User user, String title, String body,
                                     NotificationType type, Order order, OrderStatus orderStatus,
                                     Map<String, String> additionalData, String imageUrl) {
        try {
            // Build notification data - IMPORTANT: Add title/body to data for Flutter
            Map<String, String> data = new HashMap<>(additionalData);
            data.put("type", type.name().toLowerCase());
            data.put("click_action", "FLUTTER_NOTIFICATION_CLICK");
            data.put("title", title); // Flutter needs this in data payload
            data.put("body", body);   // Flutter needs this in data payload

            if (order != null) {
                data.put("orderId", order.getId().toString());
                data.put("status", orderStatus.name().toLowerCase());
            }

            // Build Android-specific config with enhanced settings
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("order_updates") // Must match Flutter's AndroidNotificationChannel
                            .setTitle(title)
                            .setBody(body)
                            .setColor("#52946B")
                            .setSound("default")
                            .setDefaultSound(true)
                            .setDefaultVibrateTimings(true)
                            .setDefaultLightSettings(true)
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .setVisibility(AndroidNotification.Visibility.PUBLIC)
                            .build())
                    .build();

            // Build APNS config for iOS with enhanced settings
            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setAlert(ApsAlert.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build())
                            .setSound("default")
                            .setBadge(1)
                            .setContentAvailable(true)
                            .build())
                    .build();

            // Build the message with BOTH notification AND data payloads
            Message message = Message.builder()
                    .setToken(user.getFcmToken())
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setImage(imageUrl)
                            .build())
                    .putAllData(data)
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .build();

            // Send message
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM notification sent successfully to user {}: {}", user.getId(), response);
            log.debug("📱 Notification - Title: '{}', Body: '{}', Type: {}, OrderId: {}",
                title, body, type, order != null ? order.getId() : "N/A");

            // Save notification history
            saveNotificationHistory(user, order, title, body, type, orderStatus, true, null);

            return true;

        } catch (FirebaseMessagingException e) {
            log.error("❌ FCM error for user {}: Code={}, Message={}",
                user.getId(),
                e.getMessagingErrorCode(),
                e.getMessage());

            // Handle invalid token
            String errorCode = e.getMessagingErrorCode() != null ? e.getMessagingErrorCode().name() : "";
            if (errorCode.contains("UNREGISTERED") || errorCode.contains("INVALID_ARGUMENT")) {
                log.warn("⚠️ Invalid FCM token for user {}. Clearing token.", user.getId());
                clearUserFcmToken(user);
            }

            // Save failed notification history
            saveNotificationHistory(user, order, title, body, type, orderStatus, false, e.getMessage());

            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error sending notification to user {}: {}", user.getId(), e.getMessage(), e);
            saveNotificationHistory(user, order, title, body, type, orderStatus, false, e.getMessage());
            return false;
        }
    }

    /**
     * Save notification to database
     */
    private void saveNotificationHistory(User user, Order order, String title, String body,
                                         NotificationType type, OrderStatus orderStatus,
                                         boolean success, String errorMessage) {
        Notification notification = Notification.builder()
                .user(user)
                .order(order)
                .title(title)
                .body(body)
                .type(type)
                .orderStatus(orderStatus)
                .sentAt(OffsetDateTime.now())
                .deliverySuccess(success)
                .errorMessage(errorMessage)
                .build();

        notificationRepository.save(notification);
    }

    /**
     * Clear invalid FCM token
     */
    @Transactional
    private void clearUserFcmToken(User user) {
        user.setFcmToken(null);
        user.setFcmPlatform(null);
        user.setFcmDeviceId(null);
        user.setFcmTokenUpdatedAt(null);
        userRepository.save(user);
    }

    /**
     * Get message content based on order status
     */
    private Map<String, String> getOrderStatusMessage(OrderStatus status, Order order) {
        Map<String, String> message = new HashMap<>();
        String orderId = order.getId().toString().substring(0, 8).toUpperCase();
        String amount = String.format("$%.2f", order.getTotalAmount());

        switch (status) {
            case PENDING:
                message.put("title", "Order Received");
                message.put("body", String.format("Your order #%s has been received. Total: %s", orderId, amount));
                break;
            case CONFIRMED:
                message.put("title", "âœ… Order Confirmed");
                message.put("body", String.format("Order #%s confirmed! Estimated time: 30 mins", orderId));
                break;
            case PREPARING:
                message.put("title", "Chef is Cooking");
                message.put("body", "Chef is preparing your delicious meal!");
                break;
            case READY:
                String storeName = order.getStore() != null ? order.getStore().getName() : "the restaurant";
                message.put("title", "Order Ready!");
                message.put("body", String.format("Order #%s is ready for pickup at %s", orderId, storeName));
                break;
            case COMPLETED:
                message.put("title", "Enjoy Your Meal!");
                message.put("body", "Order delivered! Don't forget to rate your experience");
                break;
            case CANCELLED:
                message.put("title", "Order Cancelled");
                message.put("body", String.format("Order #%s cancelled. Refund will be processed within 3-5 days", orderId));
                break;
            default:
                message.put("title", "Order Update");
                message.put("body", String.format("Your order #%s status has been updated", orderId));
        }

        return message;
    }

    /**
     * Update user FCM token
     */
    @Transactional
    public void updateFcmToken(String userId, String fcmToken, String platform, String deviceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFcmToken(fcmToken);
        user.setFcmPlatform(platform);
        user.setFcmDeviceId(deviceId);
        user.setFcmTokenUpdatedAt(OffsetDateTime.now());

        userRepository.save(user);
        log.info("âœ… FCM token updated for user {} on platform {}", userId, platform);
    }

    /**
     * Remove user FCM token (on logout)
     */
    @Transactional
    public void removeFcmToken(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        clearUserFcmToken(user);
        log.info("âœ… FCM token removed for user {}", userId);
    }
}
