package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.BaseEntity;
import com.officefood.healthy_food_api.service.CrudService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public abstract class CrudServiceImpl<T> implements CrudService<T> {

    protected abstract JpaRepository<T, String> repo();

    @Transactional(readOnly = true)
    public List<T> findAll() { return repo().findAll(); }

    @Transactional(readOnly = true)
    public Optional<T> findById(String id) { return repo().findById(id); }

    public T create(T entity) { return repo().save(entity); }

    public T update(String id, T entity) {
        // Ensure the entity has the correct ID before saving
        return repo().save(entity);
    }

    public void deleteById(String id) { repo().deleteById(id); }

    @Override
    public void softDelete(String id) {
        T entity = repo().findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).softDelete();
            repo().saveAndFlush(entity);
        }
    }

    @Override
    public void restore(String id) {
        T entity = repo().findById(id)
            .orElseThrow(() -> new RuntimeException("Entity not found with id: " + id));
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).restore();
            repo().saveAndFlush(entity);
        }
    }
}
