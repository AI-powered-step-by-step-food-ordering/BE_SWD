package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class TemplateStepResponse {
    private String id;
    private String templateId;
    private String categoryId;
    private Integer minItems;
    private Integer maxItems;
    private Double defaultQty;
    private Integer displayOrder;
}
