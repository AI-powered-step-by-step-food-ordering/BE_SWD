package com.officefood.healthy_food_api.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        String email,
        String fullName
) {}
