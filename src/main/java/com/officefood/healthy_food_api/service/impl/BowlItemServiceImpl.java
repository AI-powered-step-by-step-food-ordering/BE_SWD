package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.model.BowlItem;
import com.officefood.healthy_food_api.repository.BowlItemRepository;
import com.officefood.healthy_food_api.service.BowlItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BowlItemServiceImpl extends CrudServiceImpl<BowlItem> implements BowlItemService {
    private final BowlItemRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<BowlItem, java.util.UUID> repo() {
        return repository;
    }
}