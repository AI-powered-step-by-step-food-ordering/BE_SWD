package com.officefood.healthy_food_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Getter
    @Setter
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private ZonedDateTime createdAt;

    @Getter
    @Setter
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Getter
    @Setter
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

    /**
     * Explicit getter for boolean isActive field
     */
    public Boolean getIsActive() {
        return this.isActive;
    }

    /**
     * Explicit setter for boolean isActive field
     */
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}

