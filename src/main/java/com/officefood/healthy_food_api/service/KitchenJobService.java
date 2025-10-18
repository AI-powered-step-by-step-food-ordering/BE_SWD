package com.officefood.healthy_food_api.service;
import com.officefood.healthy_food_api.model.KitchenJob;
public interface KitchenJobService extends CrudService<KitchenJob> {
    void enqueueForOrder(java.util.UUID orderId);
    KitchenJob start(java.util.UUID jobId);
    KitchenJob finish(java.util.UUID jobId);
    KitchenJob handover(java.util.UUID jobId);}
