package com.officefood.healthy_food_api.service;

public interface TokenBlacklistService {
    void blacklist(String token);
    boolean isBlacklisted(String token);
}


