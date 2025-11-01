package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class PromotionResponse {
    private String id;
    private String code;
    private String name;
    private String type;
    private Double percentOff;
    private Double amountOff;
    private Double minOrderValue;
    private Boolean active;
    private String imageUrl;
    private ZonedDateTime createdAt;
}
