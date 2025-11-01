package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.KitchenJob;

public interface KitchenJobService extends CrudService<KitchenJob> {
    void startJob(String jobId); void handOver(String jobId); void cancelJob(String jobId);
}
