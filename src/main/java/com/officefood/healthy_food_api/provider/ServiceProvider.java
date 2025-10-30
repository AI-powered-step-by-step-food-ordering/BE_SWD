package com.officefood.healthy_food_api.provider;

import com.officefood.healthy_food_api.service.*;

public interface ServiceProvider {
    UserService users();
    TokenService tokens();
    StoreService stores();
    CategoryService categories();
    IngredientService ingredients();
    InventoryService inventories();
    PromotionService promotions();
    PromotionRedemptionService promotionRedemptions();
    OrderService orders();
    BowlTemplateService bowlTemplates();
    TemplateStepService templateSteps();
    BowlService bowls();
    BowlItemService bowlItems();
    PaymentTransactionService payments();
    KitchenJobService kitchenJobs();
    IngredientRestrictionService ingredientRestrictions();
    AuthService auth();
    EmailService email();
    FcmService fcm();
    NotificationService notifications();
}
