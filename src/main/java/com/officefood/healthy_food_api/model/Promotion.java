package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.PromotionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.*;

@Entity @Table(name="promotions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Promotion {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @Column(nullable=false, unique=true, length=50)
    private String code;

    @Column(nullable=false, length=255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private PromotionType type;

    private Double percentOff;
    private Double amountOff;
    private Double minOrderValue;

    @Column(name="starts_at") private OffsetDateTime startsAt;
    @Column(name="ends_at")   private OffsetDateTime endsAt;

    @Column(name="max_redemptions") private Integer maxRedemptions;
    @Column(name="per_order_limit") private Integer perOrderLimit;

    @Column(name="is_active", nullable=false) private Boolean isActive = true;

    @OneToMany(mappedBy = "promotion")
    private Set<PromotionRedemption> redemptions = new HashSet<>();
}
