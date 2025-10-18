package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.exception.BusinessException;
import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.*;
import com.officefood.healthy_food_api.model.enums.*;
import com.officefood.healthy_food_api.repository.*;
import com.officefood.healthy_food_api.service.OrderService;
import com.officefood.healthy_food_api.service.InventoryService;
import com.officefood.healthy_food_api.utils.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl extends CrudServiceImpl<Order> implements OrderService {
    private final OrderRepository orderRepository;
    private final BowlRepository bowlRepository;
    private final BowlItemRepository bowlItemRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final InventoryService inventoryService;
    private final KitchenJobRepository kitchenJobRepository;
@Override public Order recalcTotals(java.util.UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        double subtotal = 0d;
        for (Bowl b : bowlRepository.findByOrderId(order.getId())) {
            double line = 0d;
            for (BowlItem it : bowlItemRepository.findByBowlId(b.getId())) {
                double qty = it.getQuantity() == null ? 0d : it.getQuantity();
                double unit = it.getUnitPrice() == null ? 0d : it.getUnitPrice();
                line += qty * unit;
            }
            b.setLinePrice(MoneyUtil.round2(line));
            subtotal += line;
        }
        order.setSubtotalAmount(MoneyUtil.round2(subtotal));
        double promoTotal = 0d;
        for (PromotionRedemption red : redemptionRepository.findByOrderId(order.getId())) {
            Promotion p = red.getPromotion();
            if (p.getType() == PromotionType.PERCENT_OFF && p.getPercentOff() != null) {
                promoTotal += subtotal * (p.getPercentOff() / 100.0);
            } else if (p.getType() == PromotionType.AMOUNT_OFF && p.getAmountOff() != null) {
                promoTotal += p.getAmountOff();
            }
        }
        order.setPromotionTotal(MoneyUtil.round2(promoTotal));
        order.setTotalAmount(MoneyUtil.round2(Math.max(0, subtotal - promoTotal)));
        return orderRepository.save(order);
    }

    @Override public Order applyPromotion(java.util.UUID orderId, String promoCode) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        Promotion promo = promotionRepository.findActiveByCode(promoCode).orElseThrow(() -> new BusinessException("Promotion not found or inactive"));
        int exists = redemptionRepository.countByPromotionIdAndOrderId(promo.getId(), order.getId());
        if (exists > 0) throw new BusinessException("Promotion already applied to this order");
        double subtotal = order.getSubtotalAmount() == null ? 0d : order.getSubtotalAmount();
        if (promo.getMinOrderValue() != null && subtotal < promo.getMinOrderValue()) throw new BusinessException("Order does not meet promotion minimum value");
        PromotionRedemption red = new PromotionRedemption(); red.setPromotion(promo); red.setOrder(order);
        redemptionRepository.save(red);
        return recalcTotals(orderId);
    }

    @Override public Order confirm(java.util.UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) throw new BusinessException("Only PENDING orders can be confirmed");
        inventoryService.reserveForOrder(orderId);
        for (Bowl b : bowlRepository.findByOrderId(order.getId())) {
            KitchenJob job = new KitchenJob();
            job.setOrder(order); job.setBowl(b);
            kitchenJobRepository.save(job);
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    @Override public Order cancel(java.util.UUID orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) throw new BusinessException("Order already finalized");
        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.IN_KITCHEN || order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
            inventoryService.releaseReservationForOrder(orderId);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setNote((order.getNote() == null ? "" : order.getNote() + " ") + "[CANCELLED] " + (reason == null ? "" : reason));
        return orderRepository.save(order);
    }

    @Override public Order complete(java.util.UUID orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED) throw new BusinessException("Cancelled order cannot be completed");
        inventoryService.consumeForOrder(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        return orderRepository.save(order);
    }

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Order, java.util.UUID> repo() {
        return orderRepository;
    }
}