package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.RedemptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;

@Entity @Table(name="promotion_redemptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PromotionRedemption extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", length = 36, columnDefinition="VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="promotion_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Promotion promotion;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private RedemptionStatus status = RedemptionStatus.APPLIED;
}
