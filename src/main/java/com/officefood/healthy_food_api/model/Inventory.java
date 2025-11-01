package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.StockAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;

@Entity @Table(name="inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Inventory extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", length = 36, columnDefinition="VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="store_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Store store;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="ingredient_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private StockAction action;

    @Column(name="quantity_change")
    private Double quantityChange;

    @Column(name="balance_after")
    private Double balanceAfter;

    @Column(name="ref_order_id", columnDefinition="VARCHAR(36)")
    private String refOrderId;

    @Column(length=500)
    private String note;
}
