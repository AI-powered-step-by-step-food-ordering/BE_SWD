package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Token {
    @Id @GeneratedValue @UuidGenerator
    @Column(name="id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false, columnDefinition = "BINARY(16)")
    private User user;

    @Column(name="access_token", nullable=false, length=500)
    private String accessToken;

    @Column(name="refresh_token", length=500)
    private String refreshToken;

    @Column(name="expires_at")
    private OffsetDateTime expiresAt;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private OffsetDateTime createdAt;
}
