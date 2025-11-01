package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.*;

@Entity @Table(name="orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", length = 36, columnDefinition="VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="store_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Store store;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false, columnDefinition="VARCHAR(36)")
    private User user;


    @Column(name="pickup_at")
    private OffsetDateTime pickupAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(length=500)
    private String note;

    @Column(name="subtotal_amount")
    private Double subtotalAmount;

    @Column(name="promotion_total")
    private Double promotionTotal;

    @Column(name="total_amount")
    private Double totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Bowl> bowls = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<PaymentTransaction> payments = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<KitchenJob> kitchenJobs = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<PromotionRedemption> redemptions = new HashSet<>();
}
