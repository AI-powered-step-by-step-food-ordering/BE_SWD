package com.officefood.healthy_food_api.service;
import com.officefood.healthy_food_api.model.User;
public interface UserService extends CrudService<User> {
    java.util.Optional<User> findByEmail(String email);
}
