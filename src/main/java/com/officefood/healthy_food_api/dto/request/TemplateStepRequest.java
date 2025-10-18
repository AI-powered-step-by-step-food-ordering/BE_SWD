package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;

@Data
public class TemplateStepRequest {
    private Integer minItems;
    private Integer maxItems;
    private Double defaultQty;
    private Integer displayOrder;
    @NotNull private java.util.UUID templateId;
    @NotNull private java.util.UUID categoryId;
}
