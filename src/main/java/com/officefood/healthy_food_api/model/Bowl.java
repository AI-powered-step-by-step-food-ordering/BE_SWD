package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name="bowls")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Bowl extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, columnDefinition="BINARY(16)")
    private Order order;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="template_id", nullable=false, columnDefinition="BINARY(16)")
    private BowlTemplate template;

    @Column(length=150)
    private String name;

    @Column(length=500)
    private String instruction;

    @Column(name="line_price")
    private Double linePrice;

    @OneToMany(mappedBy = "bowl", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BowlItem> items = new HashSet<>();
}
