package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.Ingredient;
import com.officefood.healthy_food_api.repository.BowlItemRepository;
import com.officefood.healthy_food_api.repository.IngredientRepository;
import com.officefood.healthy_food_api.service.BowlItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j

@Service
@RequiredArgsConstructor
@Transactional
public class BowlItemServiceImpl extends CrudServiceImpl<BowlItem> implements BowlItemService {
    private final BowlItemRepository repository;
    private final IngredientRepository ingredientRepository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<BowlItem, String> repo() {
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

            // TÃƒÂ¡Ã‚Â»Ã‚Â± Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢ng snapshot unitPrice tÃƒÂ¡Ã‚Â»Ã‚Â« Ingredient (giÃƒÆ’Ã‚Â¡ tÃƒÂ¡Ã‚ÂºÃ‚Â¡i thÃƒÂ¡Ã‚Â»Ã‚Âi Ãƒâ€žÃ¢â‚¬ËœiÃƒÂ¡Ã‚Â»Ã†â€™m Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚ÂºÃ‚Â·t)
            if (entity.getUnitPrice() == null) {
                log.info("UnitPrice is null, fetching from Ingredient...");

                String ingredientId = entity.getIngredient().getId();
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

                // ÃƒÂ¢Ã‚Â­Ã‚Â SNAPSHOT: LÃƒâ€ Ã‚Â°u giÃƒÆ’Ã‚Â¡ hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n tÃƒÂ¡Ã‚ÂºÃ‚Â¡i cÃƒÂ¡Ã‚Â»Ã‚Â§a Ingredient
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

            // Load lại ingredient đầy đủ từ DB và set vào savedItem
            String ingredientId = savedItem.getIngredient().getId();
            log.info("Loading full Ingredient with ID: {}", ingredientId);

            Ingredient fullIngredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new NotFoundException("Ingredient not found: " + ingredientId));

            // Initialize ingredient và category để force load trước khi transaction kết thúc
            Hibernate.initialize(fullIngredient);
            log.info("Ingredient loaded: ID={}, Name={}, Unit={}, UnitPrice={}",
                fullIngredient.getId(),
                fullIngredient.getName(),
                fullIngredient.getUnit(),
                fullIngredient.getUnitPrice());

            if (fullIngredient.getCategory() != null) {
                Hibernate.initialize(fullIngredient.getCategory());
                log.info("Category loaded: ID={}, Name={}",
                    fullIngredient.getCategory().getId(),
                    fullIngredient.getCategory().getName());
            }

            // Set ingredient đã loaded vào savedItem
            savedItem.setIngredient(fullIngredient);
            log.info("=== BowlItem with full Ingredient ready to return ===");

            return savedItem;

        } catch (Exception e) {
            log.error("=== ERROR Creating BowlItem ===");
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            log.error("Full stack trace:", e);
            throw e;
        }
    }

    @Override
    public void changeQuantity(String bowlItemId, int qty) {
        repository.findById(bowlItemId).orElseThrow(); /* TODO */
    }

    @Override
    public java.util.List<BowlItem> findAllWithIngredient() {
        return repository.findAllWithIngredient();
    }

    @Override
    public java.util.Optional<BowlItem> findByIdWithIngredient(String id) {
        return repository.findByIdWithIngredient(id);
    }
}
