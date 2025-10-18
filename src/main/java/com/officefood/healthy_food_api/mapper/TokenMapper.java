package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.TokenRequest;
import com.officefood.healthy_food_api.dto.response.TokenResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface TokenMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)

    @Mapping(target = "user", expression = "java(com.officefood.healthy_food_api.mapper.helpers.UserMapperHelper.user(req.getUserId()))")
    Token toEntity(TokenRequest req);
    TokenResponse toResponse(Token entity);
}
