package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.Order;
public final class OrderMapperHelper {
    private OrderMapperHelper() { }
    public static Order order(java.util.UUID id) {
        if (id == null) return null;
        Order x = new Order();
        x.setId(id);
        return x;
    }
}
