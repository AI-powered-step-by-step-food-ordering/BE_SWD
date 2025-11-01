package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.BaseEntity;
import com.officefood.healthy_food_api.repository.base.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class SoftDeleteService {

    @Transactional
    public <T extends BaseEntity> boolean softDelete(BaseRepository<T, String> repository, String id) {
        Optional<T> entity = repository.findById(id);
        if (entity.isPresent()) {
            T e = entity.get();
            e.softDelete();
            repository.save(e);
            log.info("Soft deleted entity {} with id {}", e.getClass().getSimpleName(), id);
            return true;
        }
        return false;
    }

    @Transactional
    public <T extends BaseEntity> int softDeleteAll(BaseRepository<T, String> repository, Collection<String> ids) {
        return repository.softDeleteAllById(ids);
    }

    @Transactional
    public <T extends BaseEntity> boolean restore(BaseRepository<T, String> repository, String id) {
        Optional<T> entity = repository.findById(id);
        if (entity.isPresent()) {
            T e = entity.get();
            e.restore();
            repository.save(e);
            log.info("Restored entity {} with id {}", e.getClass().getSimpleName(), id);
            return true;
        }
        return false;
    }


    @Transactional
    public <T extends BaseEntity> int restoreAll(BaseRepository<T, String> repository, Collection<String> ids) {
        return repository.restoreAllById(ids);
    }


    public <T extends BaseEntity> boolean isActive(BaseRepository<T, String> repository, String id) {
        return repository.existsActiveById(id);
    }


    public <T extends BaseEntity> Optional<T> findActive(BaseRepository<T, String> repository, String id) {
        return repository.findActiveById(id);
    }
}

