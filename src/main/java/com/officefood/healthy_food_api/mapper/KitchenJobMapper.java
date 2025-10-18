package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.KitchenJobRequest;
import com.officefood.healthy_food_api.dto.response.KitchenJobResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.JobStatus.class })
public interface KitchenJobMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "order", expression = "java(com.officefood.healthy_food_api.mapper.helpers.OrderMapperHelper.order(req.getOrderId()))")
    @Mapping(target = "bowl", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlMapperHelper.bowl(req.getBowlId()))")
    @Mapping(target = "assignedUser", expression = "java(com.officefood.healthy_food_api.mapper.helpers.UserMapperHelper.user(req.getAssignedUserId()))")
    @Mapping(target = "status", expression = "java(JobStatus.valueOf(req.getStatus() == null ? \"QUEUED\" : req.getStatus()))")
    KitchenJob toEntity(KitchenJobRequest req);

    KitchenJobResponse toResponse(KitchenJob entity);
}
