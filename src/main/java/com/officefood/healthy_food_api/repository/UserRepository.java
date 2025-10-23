package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;

import java.util.Optional;

public interface UserRepository extends UuidJpaRepository<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    Optional<User> findByEmailVerificationToken(String token);
}
