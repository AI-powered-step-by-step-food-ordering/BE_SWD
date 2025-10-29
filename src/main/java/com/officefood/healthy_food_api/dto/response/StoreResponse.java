package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.time.ZonedDateTime;

@Data
public class StoreResponse {
    private java.util.UUID id;
    private String name;
    private String address;
    private String phone;
    private String imageUrl;
    private ZonedDateTime createdAt;
}
