package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.service.CrudService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Transactional
public abstract class CrudServiceImpl<T> implements CrudService<T> {
    // Subclasses cung cấp repository qua method này
    protected abstract JpaRepository<T, java.util.UUID> repo();

    @Override @Transactional(readOnly = true)
    public List<T> findAll() { return repo().findAll(); }

    @Override @Transactional(readOnly = true)
    public Optional<T> findById(java.util.UUID id) { return repo().findById(id); }

    @Override
    public T create(T entity) { return repo().save(entity); }

    @Override
    public T update(java.util.UUID id, T entity) { return repo().save(entity); }

    @Override
    public void deleteById(java.util.UUID id) { repo().deleteById(id); }
}
