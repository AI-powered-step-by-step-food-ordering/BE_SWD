package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.IngredientRestriction;
import com.officefood.healthy_food_api.model.enums.RestrictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IngredientRestrictionRepository extends JpaRepository<IngredientRestriction, UUID> {

    /**
     * Tìm tất cả ràng buộc EXCLUDE cho một ingredient
     */
    @Query("SELECT ir FROM IngredientRestriction ir WHERE ir.primaryIngredient.id = :ingredientId AND ir.type = :type AND ir.isActive = true")
    List<IngredientRestriction> findByPrimaryIngredientAndType(@Param("ingredientId") UUID ingredientId, @Param("type") RestrictionType type);

    /**
     * Kiểm tra xem 2 ingredients có conflict với nhau không
     */
    @Query("SELECT ir FROM IngredientRestriction ir WHERE " +
           "((ir.primaryIngredient.id = :ingredient1Id AND ir.restrictedIngredient.id = :ingredient2Id) OR " +
           " (ir.primaryIngredient.id = :ingredient2Id AND ir.restrictedIngredient.id = :ingredient1Id)) " +
           "AND ir.type = 'EXCLUDE' AND ir.isActive = true")
    List<IngredientRestriction> findConflictsBetween(@Param("ingredient1Id") UUID ingredient1Id, @Param("ingredient2Id") UUID ingredient2Id);

    /**
     * Lấy tất cả ingredients bị restrict bởi một ingredient
     */
    @Query("SELECT ir.restrictedIngredient.id FROM IngredientRestriction ir WHERE ir.primaryIngredient.id = :ingredientId AND ir.type = 'EXCLUDE' AND ir.isActive = true")
    List<UUID> findRestrictedIngredientIds(@Param("ingredientId") UUID ingredientId);
}
