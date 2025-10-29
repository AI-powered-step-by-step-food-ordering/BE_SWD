package com.officefood.healthy_food_api.model;

import com.officefood.healthy_food_api.model.enums.AccountStatus;
import com.officefood.healthy_food_api.model.enums.Role;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "goal_code", length = 50)
    private String goalCode;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "email_verification_otp", length = 6)
    private String emailVerificationOtp;

    @Column(name = "email_verification_otp_expiry")
    private OffsetDateTime emailVerificationOtpExpiry;

    @Column(name = "otp_attempts", nullable = false)
    private Integer otpAttempts = 0;

    @Column(name = "password_reset_otp", length = 6)
    private String passwordResetOtp;

    @Column(name = "password_reset_otp_expiry")
    private OffsetDateTime passwordResetOtpExpiry;

    @Column(name = "password_reset_attempts", nullable = false)
    private Integer passwordResetAttempts = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Token> tokens = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "assignedUser")
    private Set<KitchenJob> assignedJobs = new HashSet<>();

    /**
     * Override getIsActive to compute from status (not persisted in DB)
     * User table doesn't have is_active column
     */
    @Override
    @Transient
    public Boolean getIsActive() {
        return this.status == AccountStatus.ACTIVE;
    }

    /**
     * Override setIsActive to update status instead
     * Since is_active is not persisted for User, we update status
     */
    @Override
    public void setIsActive(Boolean isActive) {
        if (Boolean.TRUE.equals(isActive)) {
            this.status = AccountStatus.ACTIVE;
        } else {
            // When setting to inactive, mark as DELETED
            this.status = AccountStatus.DELETED;
        }
    }

    /**
     * Override setStatus - no need to sync isActive since it's computed
     */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /**
     * Override softDelete to set status to DELETED
     */
    @Override
    public void softDelete() {
        this.status = AccountStatus.DELETED;
        this.setDeletedAt(java.time.ZonedDateTime.now());
    }

    /**
     * Override restore to set status to ACTIVE
     */
    @Override
    public void restore() {
        this.status = AccountStatus.ACTIVE;
        this.setDeletedAt(null);
    }

    /**
     * Check if user account is truly active (both isActive and status)
     */
    public boolean isAccountActive() {
        return this.status == AccountStatus.ACTIVE && this.getDeletedAt() == null;
    }
}
