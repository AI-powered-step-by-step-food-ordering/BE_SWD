package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.Bowl;
import com.officefood.healthy_food_api.repository.BowlRepository;
import com.officefood.healthy_food_api.service.BowlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class BowlServiceImpl extends CrudServiceImpl<Bowl> implements BowlService {
    private final BowlRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<Bowl, String> repo() {
        return repository;
    }

    @Override public void markReady(String bowlId) { repository.findById(bowlId).orElseThrow(); /* TODO */ }

}
