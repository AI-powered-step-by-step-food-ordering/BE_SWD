package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.IngredientRestriction;
import com.officefood.healthy_food_api.dto.response.IngredientValidationResult;
import java.util.List;

public interface IngredientRestrictionService extends CrudService<IngredientRestriction> {

    /**
     * Kiá»ƒm tra xem cÃ³ thá»ƒ thÃªm ingredient vÃ o bowl khÃ´ng
     * @param bowlId ID cá»§a bowl
     * @param newIngredientId ID cá»§a ingredient muá»‘n thÃªm
     * @return Káº¿t quáº£ validation
     */
    IngredientValidationResult validateIngredientAddition(String bowlId, String newIngredientId);

    /**
     * Láº¥y danh sÃ¡ch ingredients bá»‹ restrict bá»Ÿi cÃ¡c ingredients Ä‘Ã£ cÃ³ trong bowl
     * @param bowlId ID cá»§a bowl
     * @return List cÃ¡c ingredient ID bá»‹ restrict
     */
    List<String> getRestrictedIngredientsForBowl(String bowlId);

    /**
     * Kiá»ƒm tra xung Ä‘á»™t giá»¯a 2 ingredients
     * @param ingredient1Id ID ingredient 1
     * @param ingredient2Id ID ingredient 2
     * @return true náº¿u cÃ³ xung Ä‘á»™t
     */
    boolean hasConflict(String ingredient1Id, String ingredient2Id);
}
