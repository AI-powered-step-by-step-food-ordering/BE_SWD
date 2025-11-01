package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.RestrictionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

/**
 * Entity để định nghĩa các ràng buộc giữa ingredients
 * Ví dụ: Cơm chiên không được có cá, Sushi không được có mayonaise
 */
@Entity @Table(name="ingredient_restrictions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class IngredientRestriction extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", length = 36, columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="primary_ingredient_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Ingredient primaryIngredient; // Ingredient chính (ví dụ: cơm chiên)

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="restricted_ingredient_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Ingredient restrictedIngredient; // Ingredient bị hạn chế (ví dụ: cá)

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private RestrictionType type; // EXCLUDE, REQUIRE, RECOMMEND

    @Column(length=500)
    private String reason; // Lý do hạn chế
}
