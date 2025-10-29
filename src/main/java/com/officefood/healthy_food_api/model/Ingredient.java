package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.*;

@Entity @Table(name="ingredients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Ingredient extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable=false, length=150)
    private String name;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="category_id", nullable=false, columnDefinition="BINARY(16)")
    private Category category;

    // Đơn vị đo (gram, ml, piece, etc.)
    @Column(name="unit", length=50)
    private String unit;

    // Số lượng chuẩn cho đơn vị (VD: 100 gram, 50 ml)
    @Column(name="standard_quantity")
    private Double standardQuantity;

    // Giá tiền cho standardQuantity đơn vị (VD: 10000 VNĐ cho 100g)
    @Column(name="unit_price")
    private Double unitPrice;

    @Column(name="image_url", length=500)
    private String imageUrl;

    @OneToMany(mappedBy = "ingredient")
    private Set<BowlItem> bowlItems = new HashSet<>();

    @OneToMany(mappedBy = "ingredient")
    private Set<Inventory> inventories = new HashSet<>();

    // Các ràng buộc khi ingredient này là primary (chính)
    @OneToMany(mappedBy = "primaryIngredient")
    private Set<IngredientRestriction> primaryRestrictions = new HashSet<>();

    // Các ràng buộc khi ingredient này bị restricted (bị hạn chế)
    @OneToMany(mappedBy = "restrictedIngredient")
    private Set<IngredientRestriction> restrictedBy = new HashSet<>();
}
