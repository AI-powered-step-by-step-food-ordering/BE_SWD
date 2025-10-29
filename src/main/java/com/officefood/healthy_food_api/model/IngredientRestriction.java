package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.RestrictionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entity để định nghĩa các ràng buộc giữa ingredients
 * Ví dụ: Cơm chiên không được có cá, Sushi không được có mayonaise
 */
@Entity @Table(name="ingredient_restrictions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class IngredientRestriction {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="primary_ingredient_id", nullable=false, columnDefinition="BINARY(16)")
    private Ingredient primaryIngredient; // Ingredient chính (ví dụ: cơm chiên)

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="restricted_ingredient_id", nullable=false, columnDefinition="BINARY(16)")
    private Ingredient restrictedIngredient; // Ingredient bị hạn chế (ví dụ: cá)

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private RestrictionType type; // EXCLUDE, REQUIRE, RECOMMEND

    @Column(length=500)
    private String reason; // Lý do hạn chế

    @Column(name="is_active", nullable=false)
    private Boolean isActive = true;
}
