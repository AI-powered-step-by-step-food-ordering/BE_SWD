package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.PromotionRequest;
import com.officefood.healthy_food_api.dto.response.PromotionResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class)
public interface PromotionMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "redemptions", ignore = true)
    Promotion toEntity(PromotionRequest req);

    @Mapping(target = "active", expression = "java(entity.getIsActive())")
    @Mapping(target = "startsAt", expression = "java(convertToZonedDateTime(entity.getStartsAt()))")
    @Mapping(target = "endsAt", expression = "java(convertToZonedDateTime(entity.getEndsAt()))")
    PromotionResponse toResponse(Promotion entity);

    default java.time.ZonedDateTime convertToZonedDateTime(java.time.OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return java.time.ZonedDateTime.ofInstant(
            offsetDateTime.toInstant(),
            java.time.ZoneId.systemDefault()
        );
    }
}
