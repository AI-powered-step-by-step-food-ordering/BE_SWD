package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.BaseEntity;
import com.officefood.healthy_food_api.repository.base.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Service hỗ trợ các thao tác soft delete cho entities
 */
@Service
@Slf4j
public class SoftDeleteService {

    /**
     * Soft delete một entity theo ID
     */
    @Transactional
    public <T extends BaseEntity> boolean softDelete(BaseRepository<T, UUID> repository, UUID id) {
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

    /**
     * Soft delete nhiều entities theo IDs
     */
    @Transactional
    public <T extends BaseEntity> int softDeleteAll(BaseRepository<T, UUID> repository, Collection<UUID> ids) {
        return repository.softDeleteAllById(ids);
    }

    /**
     * Restore một entity đã bị soft delete
     */
    @Transactional
    public <T extends BaseEntity> boolean restore(BaseRepository<T, UUID> repository, UUID id) {
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

    /**
     * Restore nhiều entities
     */
    @Transactional
    public <T extends BaseEntity> int restoreAll(BaseRepository<T, UUID> repository, Collection<UUID> ids) {
        return repository.restoreAllById(ids);
    }

    /**
     * Kiểm tra xem entity có active không
     */
    public <T extends BaseEntity> boolean isActive(BaseRepository<T, UUID> repository, UUID id) {
        return repository.existsActiveById(id);
    }

    /**
     * Tìm entity active theo ID
     */
    public <T extends BaseEntity> Optional<T> findActive(BaseRepository<T, UUID> repository, UUID id) {
        return repository.findActiveById(id);
    }
}

