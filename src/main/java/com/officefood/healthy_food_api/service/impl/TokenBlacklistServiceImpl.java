package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.service.TokenBlacklistService;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();

    @Override
    public void blacklist(String token) {
        if (token != null && !token.isBlank()) {
            blacklist.add(token);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return token != null && blacklist.contains(token);
    }
}


