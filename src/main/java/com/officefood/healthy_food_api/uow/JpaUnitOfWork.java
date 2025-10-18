package com.officefood.healthy_food_api.uow;

import com.officefood.healthy_food_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
class JpaUnitOfWork implements UnitOfWork {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryRepository inventoryRepository;
    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository promotionRedemptionRepository;
    private final OrderRepository orderRepository;
    private final BowlTemplateRepository bowlTemplateRepository;
    private final TemplateStepRepository templateStepRepository;
    private final BowlRepository bowlRepository;
    private final BowlItemRepository bowlItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final KitchenJobRepository kitchenJobRepository;

    @Override public UserRepository users() { return userRepository; }
    @Override public TokenRepository tokens() { return tokenRepository; }
    @Override public StoreRepository stores() { return storeRepository; }
    @Override public CategoryRepository categories() { return categoryRepository; }
    @Override public IngredientRepository ingredients() { return ingredientRepository; }
    @Override public InventoryRepository inventories() { return inventoryRepository; }
    @Override public PromotionRepository promotions() { return promotionRepository; }
    @Override public PromotionRedemptionRepository promotionRedemptions() { return promotionRedemptionRepository; }
    @Override public OrderRepository orders() { return orderRepository; }
    @Override public BowlTemplateRepository bowlTemplates() { return bowlTemplateRepository; }
    @Override public TemplateStepRepository templateSteps() { return templateStepRepository; }
    @Override public BowlRepository bowls() { return bowlRepository; }
    @Override public BowlItemRepository bowlItems() { return bowlItemRepository; }
    @Override public PaymentTransactionRepository payments() { return paymentTransactionRepository; }
    @Override public KitchenJobRepository kitchenJobs() { return kitchenJobRepository; }

    @Override @Transactional
    public <T> T inTransaction(Function<UnitOfWork, T> work) { return work.apply(this); }

    @Override @Transactional
    public void inTransaction(Consumer<UnitOfWork> work) { work.accept(this); }
}
