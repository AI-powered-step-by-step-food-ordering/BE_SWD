package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends CrudService<User>, UserDetailsService {
    void changePassword(String userId, String rawPassword);
}
