package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Notification;
import com.officefood.healthy_food_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserOrderBySentAtDesc(User user, Pageable pageable);

    List<Notification> findByUserAndReadAtIsNullOrderBySentAtDesc(User user);

    long countByUserAndReadAtIsNull(User user);
}

