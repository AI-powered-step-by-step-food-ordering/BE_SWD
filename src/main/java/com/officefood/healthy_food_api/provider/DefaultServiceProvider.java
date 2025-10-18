package com.officefood.healthy_food_api.provider;

import com.officefood.healthy_food_api.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DefaultServiceProvider implements ServiceProvider {
    private final UserService userService;
    private final TokenService tokenService;
    private final StoreService storeService;
    private final CategoryService categoryService;
    private final IngredientService ingredientService;
    private final InventoryService inventoryService;
    private final PromotionService promotionService;
    private final PromotionRedemptionService promotionRedemptionService;
    private final OrderService orderService;
    private final BowlTemplateService bowlTemplateService;
    private final TemplateStepService templateStepService;
    private final BowlService bowlService;
    private final BowlItemService bowlItemService;
    private final PaymentTransactionService paymentTransactionService;
    private final KitchenJobService kitchenJobService;

    @Override public UserService users() { return userService; }
    @Override public TokenService tokens() { return tokenService; }
    @Override public StoreService stores() { return storeService; }
    @Override public CategoryService categories() { return categoryService; }
    @Override public IngredientService ingredients() { return ingredientService; }
    @Override public InventoryService inventories() { return inventoryService; }
    @Override public PromotionService promotions() { return promotionService; }
    @Override public PromotionRedemptionService promotionRedemptions() { return promotionRedemptionService; }
    @Override public OrderService orders() { return orderService; }
    @Override public BowlTemplateService bowlTemplates() { return bowlTemplateService; }
    @Override public TemplateStepService templateSteps() { return templateStepService; }
    @Override public BowlService bowls() { return bowlService; }
    @Override public BowlItemService bowlItems() { return bowlItemService; }
    @Override public PaymentTransactionService payments() { return paymentTransactionService; }
    @Override public KitchenJobService kitchenJobs() { return kitchenJobService; }
}
