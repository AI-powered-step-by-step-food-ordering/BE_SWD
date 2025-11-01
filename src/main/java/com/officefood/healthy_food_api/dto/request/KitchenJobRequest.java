package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class KitchenJobRequest {
    private String status;
    private String note;
    @NotNull private String orderId;
    @NotNull private String bowlId;
    @NotNull private String assignedUserId;
}
