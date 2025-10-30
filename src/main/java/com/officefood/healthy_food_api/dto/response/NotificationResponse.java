package com.officefood.healthy_food_api.dto.response;

import com.officefood.healthy_food_api.model.enums.NotificationType;
import com.officefood.healthy_food_api.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private UUID id;
    private String title;
    private String body;
    private NotificationType type;
    private OrderStatus orderStatus;
    private UUID orderId;
    private OffsetDateTime sentAt;
    private OffsetDateTime readAt;
    private Boolean isRead;
}

