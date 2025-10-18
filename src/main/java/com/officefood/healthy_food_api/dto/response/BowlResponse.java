package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class BowlResponse {
    private java.util.UUID id;
    private java.util.UUID orderId;
    private java.util.UUID templateId;
    private String name;
    private String instruction;
    private Double linePrice;
}
