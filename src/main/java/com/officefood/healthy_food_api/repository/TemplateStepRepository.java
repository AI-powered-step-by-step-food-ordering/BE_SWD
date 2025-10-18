package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.TemplateStep;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

public interface TemplateStepRepository extends JpaRepository<TemplateStep, java.util.UUID>, TemplateStepRepositoryCustom {
    List<TemplateStep> findByTemplateIdOrderByDisplayOrderAsc(java.util.UUID templateId);

    @Modifying @Transactional
    @Query("update TemplateStep s set s.displayOrder = :display where s.id = :id")
    int updateDisplayOrder(@Param("id") java.util.UUID id, @Param("display") int display);
}
