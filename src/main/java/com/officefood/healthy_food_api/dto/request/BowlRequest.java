package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class BowlRequest {
    private String name;
    private String instruction;
    @NotNull private String orderId;
    @NotNull private String templateId;
}
