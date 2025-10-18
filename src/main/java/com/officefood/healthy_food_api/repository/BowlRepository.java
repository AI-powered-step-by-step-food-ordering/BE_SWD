package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Bowl;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface BowlRepository extends JpaRepository<Bowl, java.util.UUID>, BowlRepositoryCustom {
    List<Bowl> findByOrderId(java.util.UUID orderId);

    @Query("select coalesce(sum(b.linePrice),0) from Bowl b where b.order.id = :orderId")
    Double sumLinePrices(@Param("orderId") java.util.UUID orderId);
}
