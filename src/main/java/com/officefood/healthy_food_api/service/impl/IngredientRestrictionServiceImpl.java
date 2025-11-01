package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.dto.response.IngredientValidationResult;
import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.model.IngredientRestriction;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.repository.IngredientRestrictionRepository;
import com.officefood.healthy_food_api.service.IngredientRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IngredientRestrictionServiceImpl extends CrudServiceImpl<IngredientRestriction> implements IngredientRestrictionService {

    private final IngredientRestrictionRepository restrictionRepository;
    private final BowlRepository bowlRepository;

    @Override
    protected JpaRepository<IngredientRestriction, String> repo() {
        return restrictionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public IngredientValidationResult validateIngredientAddition(String bowlId, String newIngredientId) {
        Bowl bowl = bowlRepository.findById(bowlId)
                .orElseThrow(() -> new RuntimeException("Bowl not found"));

        // LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ ingredients hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n cÃƒÆ’Ã‚Â³ trong bowl
        List<String> existingIngredientIds = bowl.getItems().stream()
                .map(bowlItem -> bowlItem.getIngredient().getId())
                .collect(Collectors.toList());

        List<IngredientValidationResult.ConflictDetail> conflicts = new ArrayList<>();

        // KiÃƒÂ¡Ã‚Â»Ã†â€™m tra xung Ãƒâ€žÃ¢â‚¬ËœÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi tÃƒÂ¡Ã‚Â»Ã‚Â«ng ingredient Ãƒâ€žÃ¢â‚¬ËœÃƒÆ’Ã‚Â£ cÃƒÆ’Ã‚Â³
        for (String existingIngredientId : existingIngredientIds) {
            List<IngredientRestriction> conflictRestrictions = restrictionRepository
                    .findConflictsBetween(newIngredientId, existingIngredientId);

            for (IngredientRestriction restriction : conflictRestrictions) {
                String conflictingIngredientName = restriction.getPrimaryIngredient().getId().equals(existingIngredientId)
                    ? restriction.getPrimaryIngredient().getName()
                    : restriction.getRestrictedIngredient().getName();

                conflicts.add(new IngredientValidationResult.ConflictDetail(
                    existingIngredientId,
                    conflictingIngredientName,
                    restriction.getReason()
                ));
            }
        }

        if (conflicts.isEmpty()) {
            return IngredientValidationResult.valid();
        } else {
            return IngredientValidationResult.invalid(
                "Cannot add ingredient due to restrictions",
                conflicts
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRestrictedIngredientsForBowl(String bowlId) {
        Bowl bowl = bowlRepository.findById(bowlId)
                .orElseThrow(() -> new RuntimeException("Bowl not found"));

        List<String> restrictedIngredients = new ArrayList<>();

        // LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ ingredients hiÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¡n cÃƒÆ’Ã‚Â³ trong bowl
        for (BowlItem bowlItem : bowl.getItems()) {
            String ingredientId = bowlItem.getIngredient().getId();
            List<String> restricted = restrictionRepository.findRestrictedIngredientIds(ingredientId);
            restrictedIngredients.addAll(restricted);
        }

        return restrictedIngredients.stream().distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflict(String ingredient1Id, String ingredient2Id) {
        List<IngredientRestriction> conflicts = restrictionRepository
                .findConflictsBetween(ingredient1Id, ingredient2Id);
        return !conflicts.isEmpty();
    }
}
