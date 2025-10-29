package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="kitchen_jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class KitchenJob extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator
    @Column(columnDefinition="BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="order_id", nullable=false, columnDefinition="BINARY(16)")
    private Order order;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="bowl_id", nullable=false, columnDefinition="BINARY(16)")
    private Bowl bowl;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="assigned_user_id", nullable=false, columnDefinition="BINARY(16)")
    private User assignedUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private JobStatus status = JobStatus.QUEUED;

    @Column(length=500)
    private String note;


    @Column(name="started_at")  private OffsetDateTime startedAt;
    @Column(name="finished_at") private OffsetDateTime finishedAt;
    @Column(name="handed_at")   private OffsetDateTime handedAt;
}
