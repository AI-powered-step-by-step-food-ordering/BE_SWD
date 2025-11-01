package com.officefood.healthy_food_api.dto.response;

import com.officefood.healthy_food_api.model.enums.NotificationType;
import com.officefood.healthy_food_api.model.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private String title;
    private String body;
    private NotificationType type;
    private OrderStatus orderStatus;
    private String orderId;
    private OffsetDateTime sentAt;
    private OffsetDateTime readAt;
    private Boolean isRead;
}

