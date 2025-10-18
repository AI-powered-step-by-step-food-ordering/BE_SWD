package com.officefood.healthy_food_api.service.impl;
import com.officefood.healthy_food_api.exception.NotFoundException;
import com.officefood.healthy_food_api.model.*;
import com.officefood.healthy_food_api.model.enums.*;
import com.officefood.healthy_food_api.repository.KitchenJobRepository;
import com.officefood.healthy_food_api.service.KitchenJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class KitchenJobServiceImpl extends CrudServiceImpl<KitchenJob> implements KitchenJobService {
    private final KitchenJobRepository repository;
@Override public void enqueueForOrder(java.util.UUID orderId) { /* create on confirm */ }
    @Override public KitchenJob start(java.util.UUID jobId) {
        KitchenJob job = repository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        job.setStatus(JobStatus.PREPPING); job.setStartedAt(OffsetDateTime.now());
        return repository.save(job);
    }
    @Override public KitchenJob finish(java.util.UUID jobId) {
        KitchenJob job = repository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        job.setStatus(JobStatus.DONE); job.setFinishedAt(OffsetDateTime.now());
        return repository.save(job);
    }
    @Override public KitchenJob handover(java.util.UUID jobId) {
        KitchenJob job = repository.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        job.setStatus(JobStatus.HANDED_OVER); job.setHandedAt(OffsetDateTime.now());
        return repository.save(job);
    }

    @Override
    protected org.springframework.data.jpa.repository.JpaRepository<KitchenJob, java.util.UUID> repo() {
        return repository;
    }
}