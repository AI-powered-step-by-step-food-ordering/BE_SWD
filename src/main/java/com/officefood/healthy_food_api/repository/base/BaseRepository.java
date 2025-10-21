package com.officefood.healthy_food_api.repository.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Collection;
import java.util.Optional;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    int softDeleteById(ID id);
    int softDeleteAllById(Collection<ID> ids);
    int restoreById(ID id);
    int restoreAllById(Collection<ID> ids);

    Page<T> search(String keyword, Pageable pageable);

    boolean existsActiveById(ID id);
    Optional<T> findActiveById(ID id);
}
