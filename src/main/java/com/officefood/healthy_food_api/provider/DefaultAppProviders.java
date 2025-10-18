package com.officefood.healthy_food_api.provider;

import com.officefood.healthy_food_api.uow.UnitOfWork;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class DefaultAppProviders implements AppProviders {
    private final ServiceProvider services;
    private final UnitOfWork unitOfWork;
    @Override public ServiceProvider svc() { return services; }
    @Override public UnitOfWork uow() { return unitOfWork; }
}
