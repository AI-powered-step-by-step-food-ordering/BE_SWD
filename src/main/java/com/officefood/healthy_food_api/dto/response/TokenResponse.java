package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class TokenResponse {
    private java.util.UUID id;
    private java.util.UUID userId;
    private String accessToken;
    private String refreshToken;
}
