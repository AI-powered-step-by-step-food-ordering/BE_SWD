package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.KitchenJob;

public interface KitchenJobService extends CrudService<KitchenJob> {
    void startJob(java.util.UUID jobId); void handOver(java.util.UUID jobId); void cancelJob(java.util.UUID jobId);
}
