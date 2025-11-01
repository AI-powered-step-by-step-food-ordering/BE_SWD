package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Token;

public interface TokenService extends CrudService<Token> {
    void revoke(String tokenId); void revokeAllByUser(String userId);
}
