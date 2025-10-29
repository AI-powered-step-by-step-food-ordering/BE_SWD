package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.repository.BowlItemRepository;
import com.officefood.healthy_food_api.repository.IngredientRepository;
import com.officefood.healthy_food_api.service.BowlItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
@Slf4j

@Service
@RequiredArgsConstructor
@Transactional
public class BowlItemServiceImpl extends CrudServiceImpl<BowlItem> implements BowlItemService {
    private final BowlItemRepository repository;
    private final IngredientRepository ingredientRepository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<BowlItem, UUID> repo() {
        return repository;
    }

    @Override
    public BowlItem create(BowlItem entity) {
        try {
            log.info("=== START Creating BowlItem ===");

            // Validate entity
            if (entity == null) {
                log.error("BowlItem entity is null");
                throw new IllegalArgumentException("BowlItem entity cannot be null");
            }

            log.info("Entity received - Bowl ID: {}, Ingredient ID: {}, Quantity: {}, UnitPrice: {}",
                entity.getBowl() != null ? entity.getBowl().getId() : "NULL",
                entity.getIngredient() != null ? entity.getIngredient().getId() : "NULL",
                entity.getQuantity(),
                entity.getUnitPrice());

            if (entity.getIngredient() == null || entity.getIngredient().getId() == null) {
                log.error("Ingredient is null or has no ID");
                throw new IllegalArgumentException("Ingredient must be specified");
            }

            if (entity.getBowl() == null || entity.getBowl().getId() == null) {
                log.error("Bowl is null or has no ID");
                throw new IllegalArgumentException("Bowl must be specified");
            }

            // Tự động snapshot unitPrice từ Ingredient (giá tại thời điểm đặt)
            if (entity.getUnitPrice() == null) {
                log.info("UnitPrice is null, fetching from Ingredient...");

                UUID ingredientId = entity.getIngredient().getId();
                log.info("Fetching Ingredient with ID: {}", ingredientId);

                Ingredient ingredient = ingredientRepository.findById(ingredientId)
                    .orElseThrow(() -> {
                        log.error("Ingredient not found with id: {}", ingredientId);
                        return new NotFoundException("Ingredient not found with id: " + ingredientId);
                    });

                log.info("Ingredient found: ID={}, Name={}, Unit={}, StandardQty={}, UnitPrice={}",
                    ingredient.getId(),
                    ingredient.getName(),
                    ingredient.getUnit(),
                    ingredient.getStandardQuantity(),
                    ingredient.getUnitPrice());

                // ⭐ SNAPSHOT: Lưu giá hiện tại của Ingredient
                if (ingredient.getUnitPrice() == null) {
                    log.error("Ingredient unitPrice is null for ingredient: {}", ingredient.getName());
                    throw new IllegalArgumentException("Ingredient unitPrice cannot be null for: " + ingredient.getName());
                }

                entity.setUnitPrice(ingredient.getUnitPrice());
                log.info("UnitPrice set to: {}", ingredient.getUnitPrice());
            }

            log.info("Calling super.create() to save entity...");
            BowlItem savedItem = super.create(entity);
            log.info("=== SUCCESS - BowlItem created with ID: {} ===", savedItem.getId());

            return savedItem;

        } catch (Exception e) {
            log.error("=== ERROR Creating BowlItem ===");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            throw e;
        }
    }

    @Override public void changeQuantity(UUID bowlItemId, int qty) { repository.findById(bowlItemId).orElseThrow(); /* TODO */ }

}
