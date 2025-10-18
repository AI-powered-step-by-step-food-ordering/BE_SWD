package com.officefood.healthy_food_api.provider;

import com.officefood.healthy_food_api.uow.UnitOfWork;

public interface AppProviders {
    ServiceProvider svc();
    UnitOfWork uow();
}
