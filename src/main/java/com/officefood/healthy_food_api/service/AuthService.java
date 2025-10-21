package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.AuthResponse;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.RefreshTokenRequest;
import com.officefood.healthy_food_api.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refreshToken(RefreshTokenRequest req);
    void logout(String bearerToken);
}
