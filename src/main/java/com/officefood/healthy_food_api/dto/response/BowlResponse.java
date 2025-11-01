package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class BowlResponse {
    private String id;
    private String orderId;
    private String templateId;
    private String name;
    private String instruction;
    private Double linePrice;
    private ZonedDateTime createdAt;
}
