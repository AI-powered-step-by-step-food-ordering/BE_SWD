package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.IngredientKind;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name="categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Category extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable=false, length=100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private IngredientKind kind;


    @Column(name="image_url", length=500)
    private String imageUrl;

    @OneToMany(mappedBy = "category")
    private Set<Ingredient> ingredients = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<TemplateStep> templateSteps = new HashSet<>();
}
