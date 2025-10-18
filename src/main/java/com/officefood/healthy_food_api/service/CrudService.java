package com.officefood.healthy_food_api.service;
import java.util.*; import java.util.UUID;
public interface CrudService<T> {
    List<T> findAll();
    Optional<T> findById(UUID id);
    T create(T entity);
    T update(UUID id, T entity);
    void deleteById(UUID id);
}
