package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.OrderRepository;
import com.officefood.healthy_food_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl extends CrudServiceImpl<Order> implements OrderService {
    private final OrderRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Order, UUID> repo() {
        return repository;
    }

    @Override public com.officefood.healthy_food_api.model.Order recalcTotals(UUID orderId) {
        var o = repository.findById(orderId).orElseThrow();
        // TODO: use repository.calcSubtotal/order queries then save
        return repository.save(o);
    }
    @Override public com.officefood.healthy_food_api.model.Order applyPromotion(UUID orderId, String promoCode) {
        var o = repository.findById(orderId).orElseThrow();
        // TODO: lookup promotion, create redemption, recalc
        return repository.save(o);
    }
    @Override public com.officefood.healthy_food_api.model.Order confirm(UUID orderId) {
        var o = repository.findById(orderId).orElseThrow();
        // TODO: inventory reserve, job create, payment auth
        return repository.save(o);
    }
    @Override public com.officefood.healthy_food_api.model.Order cancel(UUID orderId, String reason) {
        var o = repository.findById(orderId).orElseThrow();
        // TODO: release inventory, void redemption, refund
        return repository.save(o);
    }
    @Override public com.officefood.healthy_food_api.model.Order complete(UUID orderId) {
        var o = repository.findById(orderId).orElseThrow();
        // TODO: capture payment
        return repository.save(o);
    }

}
