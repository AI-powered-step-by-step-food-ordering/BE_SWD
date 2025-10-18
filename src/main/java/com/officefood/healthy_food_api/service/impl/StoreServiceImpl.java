package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.Store;
import com.officefood.healthy_food_api.repository.StoreRepository;
import com.officefood.healthy_food_api.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreServiceImpl extends CrudServiceImpl<Store> implements StoreService {
    private final StoreRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Store, java.util.UUID> repo() {
        return repository;
    }
}