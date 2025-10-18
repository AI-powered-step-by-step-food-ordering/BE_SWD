package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.IngredientKind;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name="categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Category {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable=false, length=100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private IngredientKind kind;

    @Column(name="display_order")
    private Integer displayOrder;

    @Column(name="is_active", nullable=false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "category")
    private Set<Ingredient> ingredients = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<TemplateStep> templateSteps = new HashSet<>();
}
