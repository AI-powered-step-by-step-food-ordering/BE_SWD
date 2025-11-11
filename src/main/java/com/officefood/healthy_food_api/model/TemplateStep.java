package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "template_steps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TemplateStep extends BaseEntity {
    @Id
    @Column(name = "id", length = 36, columnDefinition="VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="template_id", nullable=false, columnDefinition="VARCHAR(36)")
    private BowlTemplate template;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="category_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Category category;

    @Column(name="min_items")
    private Integer minItems;

    @Column(name="max_items")
    private Integer maxItems;

    @Column(name="default_qty")
    private Double defaultQty;

    @Column(name="display_order")
    private Integer displayOrder;

    /**
     * JSON column lưu danh sách các ingredient ID và quantity mặc định
     * Format: [{"ingredientId": "xxx", "quantity": 100.0, "isDefault": true}, ...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_ingredients", columnDefinition = "json")
    private List<DefaultIngredientItem> defaultIngredients = new ArrayList<>();

    /**
     * Inner class để lưu thông tin ingredient mặc định
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DefaultIngredientItem {
        private String ingredientId;
        private Double quantity;
        private Boolean isDefault = true;
    }
}
