package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class KitchenJobResponse {
    private String id;
    private String orderId;
    private String bowlId;
    private String assignedUserId;
    private String status;
    private String note;
    private ZonedDateTime createdAt;
}
