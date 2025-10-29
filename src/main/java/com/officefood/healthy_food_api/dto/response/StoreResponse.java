package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class StoreResponse {
    private java.util.UUID id;
    private String name;
    private String address;
    private String phone;
    private String imageUrl; // Store image URL
}
