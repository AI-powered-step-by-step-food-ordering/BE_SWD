package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.LoginResponse;
import com.officefood.healthy_food_api.dto.EmailVerificationResponse;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.RefreshTokenRequest;
import com.officefood.healthy_food_api.dto.RegisterRequest;

public interface AuthService {
    LoginResponse register(RegisterRequest req);
    LoginResponse login(LoginRequest req);
    LoginResponse refreshToken(RefreshTokenRequest req);
    void logout(String bearerToken);
    EmailVerificationResponse verifyEmail(String token);
    EmailVerificationResponse resendVerificationEmail(String email);
}