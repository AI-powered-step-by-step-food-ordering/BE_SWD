package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.PaymentMethod;
import com.officefood.healthy_food_api.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;

@Entity @Table(name="payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", length = 36, columnDefinition="VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, columnDefinition="VARCHAR(36)")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PaymentStatus status;

    @Column
    private Double amount;

    @Column(name="provider_txn_id", length=100)
    private String providerTxnId;


    @Column(name="captured_at")
    private OffsetDateTime capturedAt;
}
