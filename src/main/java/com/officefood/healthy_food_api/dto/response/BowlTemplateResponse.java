package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;
import java.util.List;

@Data
public class BowlTemplateResponse {
    private String id;
    private String name;
    private String description;
    private Boolean active;
    private String imageUrl;
    private ZonedDateTime createdAt;
    private List<TemplateStepResponse> steps;
}
