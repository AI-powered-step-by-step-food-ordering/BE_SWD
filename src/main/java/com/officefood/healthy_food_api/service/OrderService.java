package com.officefood.healthy_food_api.service;
import com.officefood.healthy_food_api.model.Order;
public interface OrderService extends CrudService<Order> {
    Order recalcTotals(java.util.UUID orderId);
    Order applyPromotion(java.util.UUID orderId, String promoCode);
    Order confirm(java.util.UUID orderId);
    Order cancel(java.util.UUID orderId, String reason);
    Order complete(java.util.UUID orderId);}
