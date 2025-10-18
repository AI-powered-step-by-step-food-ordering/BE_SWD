package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Order;
import com.officefood.healthy_food_api.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.*;

public interface OrderRepository extends JpaRepository<Order, java.util.UUID>, OrderRepositoryCustom {
    List<Order> findByUserIdAndStatusIn(java.util.UUID userId, Collection<OrderStatus> statuses);
    List<Order> findByPlacedAtBetween(OffsetDateTime from, OffsetDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") java.util.UUID id);

    @Query("select o from Order o where o.user.id = :uid and o.status in (:st) order by o.placedAt desc")
    List<Order> listActiveByUser(@Param("uid") java.util.UUID userId, @Param("st") Collection<OrderStatus> statuses);
}
