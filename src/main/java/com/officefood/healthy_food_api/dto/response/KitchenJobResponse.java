package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class KitchenJobResponse {
    private java.util.UUID id;
    private java.util.UUID orderId;
    private java.util.UUID bowlId;
    private java.util.UUID assignedUserId;
    private String status;
    private String note;
}
