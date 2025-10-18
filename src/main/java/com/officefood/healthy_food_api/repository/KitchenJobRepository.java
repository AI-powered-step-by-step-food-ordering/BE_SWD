package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.KitchenJob;
import com.officefood.healthy_food_api.model.User;
import com.officefood.healthy_food_api.model.enums.JobStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public interface KitchenJobRepository extends JpaRepository<KitchenJob, java.util.UUID>, KitchenJobRepositoryCustom {
    List<KitchenJob> findByOrderId(java.util.UUID orderId);
    List<KitchenJob> findByAssignedUserIdAndStatus(java.util.UUID userId, JobStatus status);

    @Modifying @Transactional
    @Query("update KitchenJob k set k.assignedUser = :user where k.id = :jobId")
    int reassign(@Param("jobId") java.util.UUID jobId, @Param("user") User user);
}
