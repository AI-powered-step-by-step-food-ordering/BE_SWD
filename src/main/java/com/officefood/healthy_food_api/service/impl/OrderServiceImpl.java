package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.OrderRepository;
import com.officefood.healthy_food_api.service.OrderService;
import com.officefood.healthy_food_api.utils.IngredientCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl extends CrudServiceImpl<Order> implements OrderService {
    private final OrderRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Order, UUID> repo() {
        return repository;
    }

    @Override
    public Order recalcTotals(UUID orderId) {
        log.info("=== Recalculating totals for Order: {} ===", orderId);

        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        double subtotal = 0.0;

        log.info("Order has {} bowl(s)", order.getBowls().size());

        // Calculate each bowl's price
        for (Bowl bowl : order.getBowls()) {
            log.info("Processing Bowl: {} with {} item(s)", bowl.getId(), bowl.getItems().size());

            double bowlPrice = 0.0;

            // Calculate each bowl item's price
            for (BowlItem item : bowl.getItems()) {
                try {
                    // Calculate: (quantity / standardQuantity) × unitPrice (snapshot)
                    double itemPrice = IngredientCalculator.calculateBowlItemPrice(
                        item.getQuantity(),
                        item.getIngredient().getStandardQuantity(),
                        item.getUnitPrice()  // Use snapshot price
                    );

                    bowlPrice += itemPrice;

                    log.debug("BowlItem: ingredientId={}, quantity={}, standardQty={}, unitPrice={}, calculated={}",
                        item.getIngredient().getId(),
                        item.getQuantity(),
                        item.getIngredient().getStandardQuantity(),
                        item.getUnitPrice(),
                        itemPrice);

                } catch (Exception e) {
                    log.error("Error calculating BowlItem price: {}", e.getMessage(), e);
                    // Skip this item if error
                }
            }

            // Update bowl's line price
            bowl.setLinePrice(bowlPrice);
            subtotal += bowlPrice;

            log.info("Bowl {} total: {}", bowl.getId(), bowlPrice);
        }

        // Calculate promotion discount (if any)
        double promotionDiscount = 0.0;
        if (order.getPromotionTotal() != null) {
            promotionDiscount = order.getPromotionTotal();
        }

        // Calculate final total
        double total = subtotal - promotionDiscount;
        if (total < 0) {
            total = 0.0; // Minimum is 0
        }

        // Update order amounts
        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(total);

        log.info("Order totals - Subtotal: {}, Promotion: {}, Total: {}",
            subtotal, promotionDiscount, total);
        log.info("=== Recalculation complete ===");

        return repository.save(order);
    }

    @Override
    public Order applyPromotion(UUID orderId, String promoCode) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        // TODO: lookup promotion, create redemption, recalc
        return repository.save(order);
    }

    @Override
    public Order confirm(UUID orderId) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        // TODO: inventory reserve, job create, payment auth
        return repository.save(order);
    }

    @Override
    public Order cancel(UUID orderId, String reason) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        // TODO: release inventory, void redemption, refund
        return repository.save(order);
    }

    @Override
    public Order complete(UUID orderId) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        // TODO: capture payment
        return repository.save(order);
    }
}

