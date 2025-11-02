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

        try {
            // Step 1: Fetch order with bowls
            Order order = repository.findByIdWithBowls(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        if (order.getBowls() == null || order.getBowls().isEmpty()) {
            log.warn("Order {} has no bowls", orderId);
            order.setSubtotalAmount(0.0);
            order.setTotalAmount(0.0);
            repository.save(order);

            // Refetch with user relationship for clean response
            return repository.findByIdWithBowlsAndUser(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found after save: " + orderId));
        }

        // Step 2: Fetch bowls with items and ingredients separately to avoid Cartesian product
        java.util.List<Bowl> bowlsWithItems = repository.findBowlsWithItemsByOrderId(orderId);

        log.info("Fetched {} bowl(s) with items from database", bowlsWithItems.size());

        // Create a map for quick lookup
        java.util.Map<String, Bowl> bowlItemsMap = new java.util.HashMap<>();
        for (Bowl b : bowlsWithItems) {
            bowlItemsMap.put(b.getId(), b);
            log.debug("Bowl {} has {} item(s)", b.getId(), b.getItems() != null ? b.getItems().size() : 0);
        }

        double subtotal = 0.0;

        log.info("Order has {} bowl(s)", order.getBowls().size());

        // Calculate each bowl's price
        for (Bowl bowl : order.getBowls()) {
            if (bowl == null) {
                log.warn("Skipping null bowl");
                continue;
            }

            // Get the fully loaded bowl with items from our map
            Bowl bowlWithItems = bowlItemsMap.get(bowl.getId());
            if (bowlWithItems == null || bowlWithItems.getItems() == null || bowlWithItems.getItems().isEmpty()) {
                log.warn("Bowl {} has no items, setting linePrice to 0.0", bowl.getId());
                bowl.setLinePrice(0.0);
                // Continue to next bowl instead of adding to subtotal
                log.info("Bowl {} total: 0.0", bowl.getId());
                continue;
            }

            log.info("Processing Bowl: {} with {} item(s)", bowl.getId(), bowlWithItems.getItems().size());

            double bowlPrice = 0.0;

            // Calculate each bowl item's price
            for (BowlItem item : bowlWithItems.getItems()) {
                if (item == null) {
                    log.warn("Skipping null bowl item");
                    continue;
                }

                try {
                    // Validate required fields
                    if (item.getQuantity() == null) {
                        log.warn("BowlItem {} has null quantity, skipping", item.getId());
                        continue;
                    }

                    if (item.getUnitPrice() == null) {
                        log.warn("BowlItem {} has null unitPrice, skipping", item.getId());
                        continue;
                    }

                    if (item.getIngredient() == null) {
                        log.error("BowlItem {} has null ingredient, skipping", item.getId());
                        continue;
                    }

                    if (item.getIngredient().getStandardQuantity() == null) {
                        log.error("BowlItem {} ingredient has null standardQuantity, skipping", item.getId());
                        continue;
                    }

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
                    log.error("Error calculating BowlItem price for item {}: {}",
                        item.getId(), e.getMessage(), e);
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

            // Save the order
            repository.save(order);

            // Refetch with proper relationships for clean response
            return repository.findByIdWithBowlsAndUser(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found after save: " + orderId));

        } catch (Exception e) {
            log.error("====================================================");
            log.error("❌ ERROR in recalcTotals for Order: {}", orderId);
            log.error("====================================================");
            log.error("Exception Type: {}", e.getClass().getName());
            log.error("Exception Message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            log.error("Full Stack Trace:", e);
            log.error("====================================================");
            throw e; // Re-throw to let GlobalExceptionHandler handle it
        }
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

