package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Store;
import com.officefood.healthy_food_api.repository.base.UuidJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoreRepository extends UuidJpaRepository<Store>, JpaSpecificationExecutor<Store> {
}
