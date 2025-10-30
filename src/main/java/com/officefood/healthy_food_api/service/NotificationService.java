package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.response.NotificationResponse;
import com.officefood.healthy_food_api.model.Notification;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.NotificationRepository;
import com.officefood.healthy_food_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<Notification> notifications = notificationRepository.findByUserOrderBySentAtDesc(user, pageable);

        return notifications.map(this::mapToResponse);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserAndReadAtIsNullOrderBySentAtDesc(user);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.countByUserAndReadAtIsNull(user);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getReadAt() == null) {
            notification.setReadAt(OffsetDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findByUserAndReadAtIsNullOrderBySentAtDesc(user);

        OffsetDateTime now = OffsetDateTime.now();
        unreadNotifications.forEach(notification -> notification.setReadAt(now));

        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Map Notification entity to response DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .orderStatus(notification.getOrderStatus())
                .orderId(notification.getOrder() != null ? notification.getOrder().getId() : null)
                .sentAt(notification.getSentAt())
                .readAt(notification.getReadAt())
                .isRead(notification.getReadAt() != null)
                .build();
    }
}

