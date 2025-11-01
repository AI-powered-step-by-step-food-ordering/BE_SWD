package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.OrderRequest;
import com.officefood.healthy_food_api.dto.response.OrderResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = GlobalMapperConfig.class, uses = {BowlMapper.class})
public abstract class OrderMapper {

    @Autowired
    protected BowlMapper bowlMapper;

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
    public abstract Order toEntity(OrderRequest req);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "bowls", ignore = true)
    public abstract OrderResponse toResponse(Order entity);

    @AfterMapping
    protected void mapBowls(@MappingTarget OrderResponse response, Order entity) {
        // Only map bowls if they are initialized (fetched from database)
        if (entity.getBowls() != null && org.hibernate.Hibernate.isInitialized(entity.getBowls())) {
            response.setBowls(
                entity.getBowls().stream()
                    .map(bowlMapper::toResponse)
                    .collect(java.util.stream.Collectors.toList())
            );
        } else {
            response.setBowls(null);
        }
    }
}
