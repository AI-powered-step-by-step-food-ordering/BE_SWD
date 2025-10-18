package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class TemplateStepResponse {
    private java.util.UUID id;
    private java.util.UUID templateId;
    private java.util.UUID categoryId;
    private Integer minItems;
    private Integer maxItems;
    private Double defaultQty;
    private Integer displayOrder;
}
