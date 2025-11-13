package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class PromotionResponse {
    private String id;
    private String code;
    private String name;
    private Double discountPercent;
    private Boolean active;
    private String imageUrl;
    private ZonedDateTime createdAt;
    private ZonedDateTime startsAt;
    private ZonedDateTime endsAt;
}
