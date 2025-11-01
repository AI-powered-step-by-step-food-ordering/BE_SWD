package com.officefood.healthy_food_api.dto.response;

import lombok.*;

@Data
public class TokenResponse {
    private String id;
    private String userId;
    private String accessToken;
    private String refreshToken;
}
