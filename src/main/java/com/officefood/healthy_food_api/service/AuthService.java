package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.LoginResponse;
import com.officefood.healthy_food_api.dto.EmailVerificationResponse;
import com.officefood.healthy_food_api.dto.LoginRequest;
import com.officefood.healthy_food_api.dto.RefreshTokenRequest;
import com.officefood.healthy_food_api.dto.RegisterRequest;
import com.officefood.healthy_food_api.dto.VerifyOtpRequest;
import com.officefood.healthy_food_api.dto.ForgotPasswordRequest;
import com.officefood.healthy_food_api.dto.ResetPasswordRequest;
import com.officefood.healthy_food_api.dto.GoogleLoginRequest;

public interface AuthService {
    EmailVerificationResponse register(RegisterRequest req);
    LoginResponse login(LoginRequest req);
    LoginResponse loginWithGoogle(GoogleLoginRequest req);
    LoginResponse refreshToken(RefreshTokenRequest req);
    void logout(String bearerToken);
    EmailVerificationResponse verifyOtp(VerifyOtpRequest request);
    EmailVerificationResponse resendVerificationOtp(String email);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}