package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Order;
import java.util.List;
import java.util.UUID;

public interface OrderService extends CrudService<Order> {
    com.officefood.healthy_food_api.model.Order recalcTotals(java.util.UUID orderId); com.officefood.healthy_food_api.model.Order applyPromotion(java.util.UUID orderId, String promoCode); com.officefood.healthy_food_api.model.Order confirm(java.util.UUID orderId); com.officefood.healthy_food_api.model.Order cancel(java.util.UUID orderId, String reason); com.officefood.healthy_food_api.model.Order complete(java.util.UUID orderId);
    List<Order> findByUserId(UUID userId);
}
