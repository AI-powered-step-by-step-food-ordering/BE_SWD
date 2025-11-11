package com.officefood.healthy_food_api.dto.response;

import lombok.*;
import java.util.List;

@Data
public class TemplateStepResponse {
    private String id;
    private String templateId;
    private String categoryId;
    private CategoryResponse category;
    private Integer minItems;
    private Integer maxItems;
    private Double defaultQty;
    private Integer displayOrder;
    private List<DefaultIngredientItemDto> defaultIngredients;

    /**
     * DTO cho default ingredient item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultIngredientItemDto {
        private String ingredientId;
        private String ingredientName; // Tên ingredient để hiển thị
        private Double quantity;
        private Boolean isDefault;
        private Double unitPrice; // Giá để tính toán
        private String unit; // Đơn vị (gram, ml, piece...)
    }
}
