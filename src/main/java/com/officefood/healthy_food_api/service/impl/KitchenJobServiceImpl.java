package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.model.KitchenJob;
import com.officefood.healthy_food_api.repository.KitchenJobRepository;
import com.officefood.healthy_food_api.service.KitchenJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class KitchenJobServiceImpl extends CrudServiceImpl<KitchenJob> implements KitchenJobService {
    private final KitchenJobRepository repository;

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<KitchenJob, UUID> repo() {
        return repository;
    }

    @Override public void startJob(UUID jobId) { repository.findById(jobId).orElseThrow(); /* TODO */ }
    @Override public void handOver(UUID jobId) { repository.findById(jobId).orElseThrow(); /* TODO */ }
    @Override public void cancelJob(UUID jobId) { repository.findById(jobId).orElseThrow(); /* TODO */ }

}
