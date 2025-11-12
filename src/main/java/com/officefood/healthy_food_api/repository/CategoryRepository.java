package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Category;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository extends UuidJpaRepository<Category>, JpaSpecificationExecutor<Category> {
}
