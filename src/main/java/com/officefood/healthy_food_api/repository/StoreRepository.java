package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.Store;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface StoreRepository extends JpaRepository<Store, java.util.UUID>, StoreRepositoryCustom {
    Optional<Store> findByNameIgnoreCase(String name);

    @Query("select s from Store s " +
           "where lower(s.name) like lower(concat('%', :q, '%')) " +
           "   or lower(s.address) like lower(concat('%', :q, '%')) " +
           "order by s.name asc")
    List<Store> search(@Param("q") String keyword);
}
