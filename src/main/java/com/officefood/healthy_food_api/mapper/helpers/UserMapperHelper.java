package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.User;
public final class UserMapperHelper {
    private UserMapperHelper() { }
    public static User user(String id) {
        if (id == null) return null;
        User x = new User();
        x.setId(id);
        return x;
    }
}
