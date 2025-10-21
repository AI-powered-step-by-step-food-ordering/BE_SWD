package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends CrudService<User>, UserDetailsService {
    void changePassword(java.util.UUID userId, String rawPassword);
    void suspend(java.util.UUID userId);
    void activate(java.util.UUID userId);
}
