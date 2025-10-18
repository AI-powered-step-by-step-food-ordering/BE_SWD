package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.BowlTemplate;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface BowlTemplateRepository extends JpaRepository<BowlTemplate, java.util.UUID>, BowlTemplateRepositoryCustom {
    List<BowlTemplate> findByIsActiveTrue();
}
