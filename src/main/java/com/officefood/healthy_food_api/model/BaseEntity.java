package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Soft delete - đặt isActive = false và ghi lại thời gian xóa
     */
    public void softDelete() {
        this.isActive = false;
        this.deletedAt = ZonedDateTime.now();
    }

    /**
     * Restore - khôi phục entity đã bị soft delete
     */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}

