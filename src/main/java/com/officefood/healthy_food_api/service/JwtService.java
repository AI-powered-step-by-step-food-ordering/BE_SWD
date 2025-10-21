package com.officefood.healthy_food_api.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    String generateToken(String subject);
    String generateRefreshToken(String subject);
    boolean isRefreshTokenValid(String refreshToken, String username);
    long getAccessTokenExpiration();
    long getRefreshTokenExpiration();
}
