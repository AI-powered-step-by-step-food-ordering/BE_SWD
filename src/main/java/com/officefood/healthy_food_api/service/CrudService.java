package com.officefood.healthy_food_api.service;

import java.util.List;
import java.util.Optional;

public interface CrudService<T> {
    List<T> findAll();
    Optional<T> findById(String id);
    T create(T entity);
    T update(String id, T entity);
    void deleteById(String id);
    void softDelete(String id);
    void restore(String id);
}
