package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name="users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id @GeneratedValue
    private UUID id;

    @Column(name="full_name", nullable=false, length=255)
    private String fullName;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Column(name="company_name", length=255)
    private String companyName;

    @Column(name="goal_code", length=50)
    private String goalCode;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name="created_at", updatable=false)
    private OffsetDateTime createdAt;
}
