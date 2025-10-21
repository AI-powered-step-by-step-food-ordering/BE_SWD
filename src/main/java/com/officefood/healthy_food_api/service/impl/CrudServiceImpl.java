package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.service.CrudService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Transactional
public abstract class CrudServiceImpl<T> implements CrudService<T> {

    protected abstract JpaRepository<T, UUID> repo();

    @Transactional(readOnly = true)
    public List<T> findAll() { return repo().findAll(); }

    @Transactional(readOnly = true)
    public Optional<T> findById(UUID id) { return repo().findById(id); }

    public T create(T entity) { return repo().save(entity); }

    public T update(UUID id, T entity) { return repo().save(entity); }

    public void deleteById(UUID id) { repo().deleteById(id); }
}
