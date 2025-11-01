package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;

@Entity @Table(name = "tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Token extends BaseEntity {
    @Id @GeneratedValue @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name="id", length = 36, columnDefinition = "VARCHAR(36)")
    private String id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false, columnDefinition = "VARCHAR(36)")
    private User user;

    @Column(name="access_token", nullable=false, length=500)
    private String accessToken;

    @Column(name="refresh_token", length=500)
    private String refreshToken;

    @Column(name="expires_at")
    private OffsetDateTime expiresAt;
}
