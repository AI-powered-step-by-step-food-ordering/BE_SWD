package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class BowlRequest {
    private String name;
    private String instruction;
    @NotNull private java.util.UUID orderId;
    @NotNull private java.util.UUID templateId;
}
