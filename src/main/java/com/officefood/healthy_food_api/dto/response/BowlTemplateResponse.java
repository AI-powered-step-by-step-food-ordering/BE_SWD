package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class BowlTemplateResponse {
    private java.util.UUID id;
    private String name;
    private String description;
    private Boolean active;
    private String imageUrl;
    private ZonedDateTime createdAt;
}
