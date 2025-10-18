package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class BowlTemplateRequest {
    @NotBlank private String name;
    private String description;
    private Boolean isActive;
}
