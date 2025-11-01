package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.OrderRepository;
import com.officefood.healthy_food_api.service.FcmService;
import com.officefood.healthy_food_api.service.OrderService;
import com.officefood.healthy_food_api.utils.IngredientCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl extends CrudServiceImpl<Order> implements OrderService {
    private final OrderRepository repository;
    private final FcmService fcmService;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Order, String> repo() {
        return repository;
    }

    @Override
    public Order recalcTotals(String orderId) {
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
                    // Calculate: (quantity / standardQuantity) ÃƒÆ’Ã¢â‚¬â€ unitPrice (snapshot)
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
    public Order applyPromotion(String orderId, String promoCode) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        // TODO: lookup promotion, create redemption, recalc
        return repository.save(order);
    }

    @Override
    public Order confirm(String orderId) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Update order status to CONFIRMED
        order.setStatus(com.officefood.healthy_food_api.model.enums.OrderStatus.CONFIRMED);

        // TODO: inventory reserve, job create, payment auth

        // Send push notification
        try {
            fcmService.sendOrderNotification(order, com.officefood.healthy_food_api.model.enums.OrderStatus.CONFIRMED);
        } catch (Exception e) {
            log.error("Failed to send order confirmation notification: {}", e.getMessage());
        }

        return repository.save(order);
    }

    @Override
    public Order cancel(String orderId, String reason) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Update order status to CANCELLED
        order.setStatus(com.officefood.healthy_food_api.model.enums.OrderStatus.CANCELLED);

        // TODO: release inventory, void redemption, refund
        // TODO: Store cancellation reason if needed

        // Send push notification
        try {
            fcmService.sendOrderNotification(order, com.officefood.healthy_food_api.model.enums.OrderStatus.CANCELLED);
        } catch (Exception e) {
            log.error("Failed to send order cancellation notification: {}", e.getMessage());
        }

        return repository.save(order);
    }

    @Override
    public Order complete(String orderId) {
        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Update order status to COMPLETED
        order.setStatus(com.officefood.healthy_food_api.model.enums.OrderStatus.COMPLETED);

        // TODO: capture payment

        // Send push notification
        try {
            fcmService.sendOrderNotification(order, com.officefood.healthy_food_api.model.enums.OrderStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Failed to send order completion notification: {}", e.getMessage());
        }

        return repository.save(order);
    }

    @Override
    public java.util.List<Order> findByUserId(String userId) {
        log.info("Finding orders for user: {}", userId);
        return repository.findByUserId(userId);
    }

    @Override
    public java.util.List<Order> findAllWithBowlsAndUser() {
        log.info("Finding all orders with bowls and user data");
        java.util.List<Order> orders = repository.findAllWithBowlsAndUser();

        // Fetch bowl templates separately to avoid Cartesian product
        if (!orders.isEmpty()) {
            java.util.List<String> bowlIds = orders.stream()
                    .flatMap(o -> o.getBowls().stream())
                    .map(Bowl::getId)
                    .distinct()
                    .toList();

            if (!bowlIds.isEmpty()) {
                repository.fetchBowlTemplates(bowlIds);
            }
        }

        return orders;
    }

    @Override
    public java.util.Optional<Order> findByIdWithBowlsAndUser(String id) {
        log.info("Finding order by id {} with bowls and user data", id);
        return repository.findByIdWithBowlsAndUser(id);
    }

    @Override
    public java.util.List<Order> findByUserIdWithBowlsAndUser(String userId) {
        log.info("Finding orders for user {} with bowls and user data", userId);
        java.util.List<Order> orders = repository.findByUserIdWithBowlsAndUser(userId);

        // Fetch bowl templates separately to avoid Cartesian product
        if (!orders.isEmpty()) {
            java.util.List<String> bowlIds = orders.stream()
                    .flatMap(o -> o.getBowls().stream())
                    .map(Bowl::getId)
                    .distinct()
                    .toList();

            if (!bowlIds.isEmpty()) {
                repository.fetchBowlTemplates(bowlIds);
            }
        }

        return orders;
    }
}

