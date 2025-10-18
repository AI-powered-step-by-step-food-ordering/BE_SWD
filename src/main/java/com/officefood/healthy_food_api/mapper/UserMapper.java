package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.UserRequest;
import com.officefood.healthy_food_api.dto.response.UserResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.Role.class, com.officefood.healthy_food_api.model.enums.AccountStatus.class })
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "assignedJobs", ignore = true)
    @Mapping(target = "role", expression = "java(Role.valueOf(req.getRole() == null ? \"USER\" : req.getRole()))")
    @Mapping(target = "status", expression = "java(AccountStatus.valueOf(req.getStatus() == null ? \"ACTIVE\" : req.getStatus()))")
    User toEntity(UserRequest req);

    UserResponse toResponse(User entity);
}
