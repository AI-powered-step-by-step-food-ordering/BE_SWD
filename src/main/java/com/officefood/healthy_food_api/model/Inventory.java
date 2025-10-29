package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.StockAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Inventory extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="store_id", nullable=false, columnDefinition="BINARY(16)")
    private Store store;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="ingredient_id", nullable=false, columnDefinition="BINARY(16)")
    private Ingredient ingredient;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private StockAction action;

    @Column(name="quantity_change")
    private Double quantityChange;

    @Column(name="balance_after")
    private Double balanceAfter;

    @Column(name="ref_order_id", columnDefinition="BINARY(16)")
    private UUID refOrderId;

    @Column(length=500)
    private String note;
}
