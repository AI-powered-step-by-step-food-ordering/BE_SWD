package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.OrderRequest;
import com.officefood.healthy_food_api.dto.response.OrderResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface OrderMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", expression = "java(com.officefood.healthy_food_api.mapper.helpers.StoreMapperHelper.store(req.getStoreId()))")
    @Mapping(target = "user", expression = "java(com.officefood.healthy_food_api.mapper.helpers.UserMapperHelper.user(req.getUserId()))")
    @Mapping(target = "bowls", ignore = true)
    @Mapping(target = "payments", ignore = true)
    @Mapping(target = "kitchenJobs", ignore = true)
    @Mapping(target = "redemptions", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "subtotalAmount", ignore = true)
    @Mapping(target = "promotionTotal", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    Order toEntity(OrderRequest req);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "userId", source = "user.id")
    OrderResponse toResponse(Order entity);
}
