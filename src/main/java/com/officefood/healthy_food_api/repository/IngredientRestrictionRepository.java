package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.IngredientRestriction;
import com.officefood.healthy_food_api.model.enums.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRestrictionRepository extends JpaRepository<IngredientRestriction, String> {

    /**
     * TÃƒÆ’Ã‚Â¬m tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ rÃƒÆ’Ã‚Â ng buÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢c EXCLUDE cho mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t ingredient
     */
    @Query("SELECT ir FROM IngredientRestriction ir WHERE ir.primaryIngredient.id = :ingredientId AND ir.type = :type AND ir.isActive = true")
    List<IngredientRestriction> findByPrimaryIngredientAndType(@Param("ingredientId") String ingredientId, @Param("type") RestrictionType type);

    /**
     * KiÃƒÂ¡Ã‚Â»Ã†â€™m tra xem 2 ingredients cÃƒÆ’Ã‚Â³ conflict vÃƒÂ¡Ã‚Â»Ã¢â‚¬Âºi nhau khÃƒÆ’Ã‚Â´ng
     */
    @Query("SELECT ir FROM IngredientRestriction ir WHERE " +
           "((ir.primaryIngredient.id = :ingredient1Id AND ir.restrictedIngredient.id = :ingredient2Id) OR " +
           " (ir.primaryIngredient.id = :ingredient2Id AND ir.restrictedIngredient.id = :ingredient1Id)) " +
           "AND ir.type = 'EXCLUDE' AND ir.isActive = true")
    List<IngredientRestriction> findConflictsBetween(@Param("ingredient1Id") String ingredient1Id, @Param("ingredient2Id") String ingredient2Id);

    /**
     * LÃƒÂ¡Ã‚ÂºÃ‚Â¥y tÃƒÂ¡Ã‚ÂºÃ‚Â¥t cÃƒÂ¡Ã‚ÂºÃ‚Â£ ingredients bÃƒÂ¡Ã‚Â»Ã¢â‚¬Â¹ restrict bÃƒÂ¡Ã‚Â»Ã…Â¸i mÃƒÂ¡Ã‚Â»Ã¢â€žÂ¢t ingredient
     */
    @Query("SELECT ir.restrictedIngredient.id FROM IngredientRestriction ir WHERE ir.primaryIngredient.id = :ingredientId AND ir.type = 'EXCLUDE' AND ir.isActive = true")
    List<String> findRestrictedIngredientIds(@Param("ingredientId") String ingredientId);
}
