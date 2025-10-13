package com.officefood.healthy_food_api.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    String generateToken(String subject);
}


