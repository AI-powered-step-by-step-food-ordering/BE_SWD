package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Token;
import com.officefood.healthy_food_api.repository.TokenRepository;
import com.officefood.healthy_food_api.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class TokenServiceImpl extends CrudServiceImpl<Token> implements TokenService {
    private final TokenRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Token, String> repo() {
        return repository;
    }

    @Override public void revoke(String tokenId) { repository.findById(tokenId).orElseThrow(); /* TODO */ }
    @Override public void revokeAllByUser(String userId) { /* TODO */ }

}
