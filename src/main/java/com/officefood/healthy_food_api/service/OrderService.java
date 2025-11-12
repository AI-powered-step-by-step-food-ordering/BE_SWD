package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.request.OrderSearchRequest;
import com.officefood.healthy_food_api.model.Order;
import java.util.List;
import java.util.Optional;

public interface OrderService extends CrudService<Order> {
    com.officefood.healthy_food_api.model.Order recalcTotals(String orderId); com.officefood.healthy_food_api.model.Order applyPromotion(String orderId, String promoCode); com.officefood.healthy_food_api.model.Order confirm(String orderId); com.officefood.healthy_food_api.model.Order cancel(String orderId, String reason); com.officefood.healthy_food_api.model.Order complete(String orderId);
    List<Order> findByUserId(String userId);
    List<Order> findAllWithBowlsAndUser();
    Optional<Order> findByIdWithBowlsAndUser(String id);
    List<Order> findByUserIdWithBowlsAndUser(String userId);

    // Search functionality
    List<Order> search(OrderSearchRequest searchRequest);
}
