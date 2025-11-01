package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientValidationResult {
    private boolean isValid;
    private String message;
    private List<ConflictDetail> conflicts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictDetail {
        private String conflictingIngredientId;
        private String conflictingIngredientName;
        private String reason;
    }

    public static IngredientValidationResult valid() {
        return IngredientValidationResult.builder()
                .isValid(true)
                .message("Ingredient can be added")
                .build();
    }

    public static IngredientValidationResult invalid(String message, List<ConflictDetail> conflicts) {
        return IngredientValidationResult.builder()
                .isValid(false)
                .message(message)
                .conflicts(conflicts)
                .build();
    }
}
