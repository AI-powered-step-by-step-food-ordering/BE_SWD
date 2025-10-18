package com.officefood.healthy_food_api.repository;

import com.officefood.healthy_food_api.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface UserRepository extends JpaRepository<User, java.util.UUID>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("select u from User u " +
           "where lower(u.fullName) like lower(concat('%', :q, '%')) " +
           "   or lower(u.email) like lower(concat('%', :q, '%')) " +
           "order by u.createdAt desc")
    List<User> searchByNameOrEmail(@Param("q") String keyword, Pageable pageable);
}
