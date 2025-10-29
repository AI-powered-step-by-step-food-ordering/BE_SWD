package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity @Table(name="template_steps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TemplateStep extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private java.util.UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="template_id", nullable=false, columnDefinition="BINARY(16)")
    private BowlTemplate template;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="category_id", nullable=false, columnDefinition="BINARY(16)")
    private Category category;

    @Column(name="min_items")
    private Integer minItems;

    @Column(name="max_items")
    private Integer maxItems;

    @Column(name="default_qty")
    private Double defaultQty;

    @Column(name="display_order")
    private Integer displayOrder;
}
