package com.officefood.healthy_food_api.uow;

import com.officefood.healthy_food_api.repository.*;
import java.util.function.Function;
import java.util.function.Consumer;

public interface UnitOfWork {
    UserRepository users();
    TokenRepository tokens();
    StoreRepository stores();
    CategoryRepository categories();
    IngredientRepository ingredients();
    InventoryRepository inventories();
    PromotionRepository promotions();
    PromotionRedemptionRepository promotionRedemptions();
    OrderRepository orders();
    BowlTemplateRepository bowlTemplates();
    TemplateStepRepository templateSteps();
    BowlRepository bowls();
    BowlItemRepository bowlItems();
    PaymentTransactionRepository payments();
    KitchenJobRepository kitchenJobs();

    <T> T inTransaction(Function<UnitOfWork, T> work);
    void inTransaction(Consumer<UnitOfWork> work);
}
