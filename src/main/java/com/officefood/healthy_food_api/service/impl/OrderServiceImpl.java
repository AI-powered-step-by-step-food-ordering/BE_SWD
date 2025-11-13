package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.request.OrderSearchRequest;
import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.repository.OrderRepository;
import com.officefood.healthy_food_api.service.FcmService;
import com.officefood.healthy_food_api.service.OrderService;
import com.officefood.healthy_food_api.specification.OrderSpecifications;
import com.officefood.healthy_food_api.utils.IngredientCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl extends CrudServiceImpl<Order> implements OrderService {
    private final OrderRepository repository;
    private final BowlRepository bowlRepository;
    private final FcmService fcmService;
            private final com.officefood.healthy_food_api.repository.PromotionRepository promotionRepository;
    private final com.officefood.healthy_food_api.repository.PromotionRedemptionRepository promotionRedemptionRepository;

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

        // Calculate promotion discount from database (query PromotionRedemptions)
        double promotionDiscount = repository.calcTotalDiscount(orderId);

        // Calculate final total
        double total = subtotal - promotionDiscount;
        if (total < 0) {
            total = 0.0; // Minimum is 0
        }

        // Update order amounts
        order.setSubtotalAmount(subtotal);
        order.setPromotionTotal(promotionDiscount);  // Update promotionTotal
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
        log.info("Applying promotion {} to order {}", promoCode, orderId);

        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Kiểm tra xem order đã được confirm chưa - nếu rồi thì không cho đổi promotion
        if (order.getStatus() != com.officefood.healthy_food_api.model.enums.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot change promotion for order with status: " + order.getStatus());
        }

        // Tìm promotion theo code (uppercase)
        com.officefood.healthy_food_api.model.Promotion promotion =
            promotionRepository.findByCodeAndIsActiveTrue(promoCode.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Promotion not found or inactive: " + promoCode));

        // Validate thời gian hiệu lực
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();
        if (promotion.getStartsAt() != null && promotion.getStartsAt().isAfter(now)) {
            throw new IllegalArgumentException("Promotion has not started yet");
        }
        if (promotion.getEndsAt() != null && promotion.getEndsAt().isBefore(now)) {
            throw new IllegalArgumentException("Promotion has expired");
        }

        // Tìm tất cả promotion redemptions đang APPLIED cho order này
        java.util.List<com.officefood.healthy_food_api.model.PromotionRedemption> existingRedemptions =
            promotionRedemptionRepository.findAll().stream()
                .filter(r -> r.getOrder().getId().equals(orderId) &&
                            r.getStatus() == com.officefood.healthy_food_api.model.enums.RedemptionStatus.APPLIED)
                .collect(java.util.stream.Collectors.toList());

        // Void tất cả promotion cũ (cho phép thay đổi promotion)
        for (com.officefood.healthy_food_api.model.PromotionRedemption oldRedemption : existingRedemptions) {
            oldRedemption.setStatus(com.officefood.healthy_food_api.model.enums.RedemptionStatus.VOIDED);
            promotionRedemptionRepository.save(oldRedemption);
            log.info("Voided old promotion {} for order {}",
                oldRedemption.getPromotion().getCode(), orderId);
        }

        // Tạo PromotionRedemption mới
        com.officefood.healthy_food_api.model.PromotionRedemption redemption =
            new com.officefood.healthy_food_api.model.PromotionRedemption();
        redemption.setPromotion(promotion);
        redemption.setOrder(order);
        redemption.setStatus(com.officefood.healthy_food_api.model.enums.RedemptionStatus.APPLIED);

        promotionRedemptionRepository.save(redemption);

        log.info("Promotion {} applied to order {}. Recalculating totals...", promoCode, orderId);

        // Recalc totals
        return recalcTotals(orderId);
    }

    @Override
    public Order removePromotion(String orderId) {
        log.info("Removing all promotions from order {}", orderId);

        Order order = repository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));

        // Kiểm tra xem order đã được confirm chưa
        if (order.getStatus() != com.officefood.healthy_food_api.model.enums.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Cannot remove promotion for order with status: " + order.getStatus());
        }

        // Tìm tất cả promotion redemptions đang APPLIED cho order này
        java.util.List<com.officefood.healthy_food_api.model.PromotionRedemption> existingRedemptions =
            promotionRedemptionRepository.findAll().stream()
                .filter(r -> r.getOrder().getId().equals(orderId) &&
                            r.getStatus() == com.officefood.healthy_food_api.model.enums.RedemptionStatus.APPLIED)
                .collect(java.util.stream.Collectors.toList());

        if (existingRedemptions.isEmpty()) {
            log.info("No active promotions found for order {}", orderId);
            return order; // Không có promotion nào để remove
        }

        // Void tất cả promotions
        for (com.officefood.healthy_food_api.model.PromotionRedemption redemption : existingRedemptions) {
            redemption.setStatus(com.officefood.healthy_food_api.model.enums.RedemptionStatus.VOIDED);
            promotionRedemptionRepository.save(redemption);
            log.info("Voided promotion {} for order {}", redemption.getPromotion().getCode(), orderId);
        }

        log.info("All promotions removed from order {}. Recalculating totals...", orderId);

        // Recalc totals (sẽ tính lại với promotionTotal = 0)
        return recalcTotals(orderId);
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
    @Transactional(readOnly = true)
    public java.util.Optional<Order> findByIdWithBowlsAndUser(String id) {
        log.info("Finding order by id {} with bowls and user data", id);
        java.util.Optional<Order> orderOpt = repository.findByIdWithBowlsAndUser(id);

        if (orderOpt.isEmpty()) {
            return orderOpt;
        }

        Order order = orderOpt.get();

        // Fetch bowl items separately to avoid Cartesian product
        if (order.getBowls() != null && !order.getBowls().isEmpty()) {
            java.util.List<String> bowlIds = order.getBowls().stream()
                .map(Bowl::getId)
                .distinct()
                .toList();

            if (!bowlIds.isEmpty()) {
                // Fetch bowl items
                java.util.List<Bowl> bowlsWithItems = bowlRepository.findByIdsWithItems(bowlIds);
                java.util.Map<String, Bowl> bowlItemsMap = bowlsWithItems.stream()
                    .collect(java.util.stream.Collectors.toMap(Bowl::getId, b -> b));

                // Update bowls with items
                for (Bowl bowl : order.getBowls()) {
                    Bowl enriched = bowlItemsMap.get(bowl.getId());
                    if (enriched != null && enriched.getItems() != null) {
                        bowl.setItems(enriched.getItems());
                    }
                }

                // Fetch templates with steps
                java.util.List<Bowl> bowlsWithTemplates = repository.fetchBowlTemplates(bowlIds);
                java.util.Map<String, Bowl> bowlTemplateMap = bowlsWithTemplates.stream()
                    .collect(java.util.stream.Collectors.toMap(Bowl::getId, b -> b));

                // Update bowls with templates (with steps)
                for (Bowl bowl : order.getBowls()) {
                    Bowl enriched = bowlTemplateMap.get(bowl.getId());
                    if (enriched != null && enriched.getTemplate() != null) {
                        bowl.setTemplate(enriched.getTemplate());
                        // Force initialize template steps
                        if (enriched.getTemplate().getSteps() != null) {
                            org.hibernate.Hibernate.initialize(enriched.getTemplate().getSteps());
                        }
                    }
                }
            }
        }

        return java.util.Optional.of(order);
    }

    @Override
    public java.util.List<Order> findByUserIdWithBowlsAndUser(String userId) {
        log.info("Finding orders for user {} with bowls and user data", userId);
        java.util.List<Order> orders = repository.findByUserIdWithBowlsAndUser(userId);

        // Fetch bowl templates WITH STEPS separately to avoid Cartesian product
        if (!orders.isEmpty()) {
            java.util.List<String> bowlIds = orders.stream()
                    .flatMap(o -> o.getBowls().stream())
                    .map(Bowl::getId)
                    .distinct()
                    .toList();

            if (!bowlIds.isEmpty()) {
                // Fetch templates with steps
                java.util.List<Bowl> bowlsWithTemplates = repository.fetchBowlTemplates(bowlIds);

                // Create a map for quick lookup
                java.util.Map<String, Bowl> bowlMap = bowlsWithTemplates.stream()
                    .collect(java.util.stream.Collectors.toMap(Bowl::getId, b -> b));

                // Update bowls in orders with fetched templates
                for (Order order : orders) {
                    for (Bowl bowl : order.getBowls()) {
                        Bowl enriched = bowlMap.get(bowl.getId());
                        if (enriched != null && enriched.getTemplate() != null) {
                            bowl.setTemplate(enriched.getTemplate());
                        }
                    }
                }
            }
        }

        return orders;
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<Order> search(OrderSearchRequest searchRequest) {
        log.info("Searching orders with criteria: {}", searchRequest);

        // Build specification from search request
        Specification<Order> spec = OrderSpecifications.withSearchCriteria(searchRequest);

        // Execute search
        java.util.List<Order> orders = repository.findAll(spec);

        log.info("Found {} orders matching search criteria", orders.size());
        return orders;
    }
}

