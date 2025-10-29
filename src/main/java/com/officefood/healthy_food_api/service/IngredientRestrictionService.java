package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.IngredientRestriction;
import com.officefood.healthy_food_api.dto.response.IngredientValidationResult;
import java.util.List;
import java.util.UUID;

public interface IngredientRestrictionService extends CrudService<IngredientRestriction> {

    /**
     * Kiểm tra xem có thể thêm ingredient vào bowl không
     * @param bowlId ID của bowl
     * @param newIngredientId ID của ingredient muốn thêm
     * @return Kết quả validation
     */
    IngredientValidationResult validateIngredientAddition(UUID bowlId, UUID newIngredientId);

    /**
     * Lấy danh sách ingredients bị restrict bởi các ingredients đã có trong bowl
     * @param bowlId ID của bowl
     * @return List các ingredient ID bị restrict
     */
    List<UUID> getRestrictedIngredientsForBowl(UUID bowlId);

    /**
     * Kiểm tra xung đột giữa 2 ingredients
     * @param ingredient1Id ID ingredient 1
     * @param ingredient2Id ID ingredient 2
     * @return true nếu có xung đột
     */
    boolean hasConflict(UUID ingredient1Id, UUID ingredient2Id);
}
