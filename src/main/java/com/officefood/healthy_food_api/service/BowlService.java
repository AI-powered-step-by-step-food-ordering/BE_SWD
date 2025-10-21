package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.model.Bowl;

public interface BowlService extends CrudService<Bowl> {
    void markReady(java.util.UUID bowlId);
}
