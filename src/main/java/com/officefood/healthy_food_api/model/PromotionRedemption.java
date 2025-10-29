package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.RedemptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="promotion_redemptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PromotionRedemption extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="promotion_id", nullable=false, columnDefinition="BINARY(16)")
    private Promotion promotion;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, columnDefinition="BINARY(16)")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private RedemptionStatus status = RedemptionStatus.APPLIED;
}
