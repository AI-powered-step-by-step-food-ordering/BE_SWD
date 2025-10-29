package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.PaymentMethod;
import com.officefood.healthy_food_api.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="payment_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PaymentTransaction extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, columnDefinition="BINARY(16)")
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
