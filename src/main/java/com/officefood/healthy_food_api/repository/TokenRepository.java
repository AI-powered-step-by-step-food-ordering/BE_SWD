package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Token;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public interface TokenRepository extends JpaRepository<Token, java.util.UUID>, TokenRepositoryCustom {
    List<Token> findByUserId(java.util.UUID userId);
    Optional<Token> findByAccessToken(String accessToken);

    @Modifying @Transactional
    @Query("delete from Token t where t.expiresAt is not null and t.expiresAt < CURRENT_TIMESTAMP")
    int deleteExpired();
}
