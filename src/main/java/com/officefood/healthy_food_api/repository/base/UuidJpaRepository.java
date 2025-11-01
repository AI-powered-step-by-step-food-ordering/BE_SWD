package com.officefood.healthy_food_api.repository.base;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface UuidJpaRepository<T> extends BaseRepository<T, String> { }
