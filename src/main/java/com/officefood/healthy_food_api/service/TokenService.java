package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Token;

public interface TokenService extends CrudService<Token> {
    void revoke(java.util.UUID tokenId); void revokeAllByUser(java.util.UUID userId);
}
