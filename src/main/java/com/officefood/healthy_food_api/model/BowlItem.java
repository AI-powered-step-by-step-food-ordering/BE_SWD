package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name="bowl_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class BowlItem {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="bowl_id", nullable=false, columnDefinition="BINARY(16)")
    private Bowl bowl;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="ingredient_id", nullable=false, columnDefinition="BINARY(16)")
    private Ingredient ingredient;

    @Column
    private Double quantity;

    @Column(name="unit_price")
    private Double unitPrice;
}
