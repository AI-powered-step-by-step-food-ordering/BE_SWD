package com.officefood.healthy_food_api.dto.request;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class TemplateStepRequest {
    private Integer minItems;
    private Integer maxItems;
    private Double defaultQty;
    private Integer displayOrder;
    @NotNull private String templateId;
    @NotNull private String categoryId;
    private List<DefaultIngredientItemRequest> defaultIngredients;

    /**
     * DTO cho default ingredient item trong request
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultIngredientItemRequest {
        @NotNull private String ingredientId;
        @NotNull private Double quantity;
        private Boolean isDefault; // Mặc định là true nếu không truyền
    }
}
