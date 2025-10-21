package com.officefood.healthy_food_api.config;

import com.officefood.healthy_food_api.repository.base.BaseRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.officefood.healthy_food_api.repository",
        repositoryBaseClass = BaseRepositoryImpl.class
)
public class JpaConfig { }
