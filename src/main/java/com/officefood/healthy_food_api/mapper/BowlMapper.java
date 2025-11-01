package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.BowlRequest;
import com.officefood.healthy_food_api.dto.response.BowlResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = GlobalMapperConfig.class, uses = {BowlTemplateMapper.class, BowlItemMapper.class})
public abstract class BowlMapper {

    @Autowired
    protected BowlItemMapper bowlItemMapper;

    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", expression = "java(com.officefood.healthy_food_api.mapper.helpers.OrderMapperHelper.order(req.getOrderId()))")
    @Mapping(target = "template", expression = "java(com.officefood.healthy_food_api.mapper.helpers.BowlTemplateMapperHelper.bowlTemplate(req.getTemplateId()))")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "linePrice", ignore = true)
    public abstract Bowl toEntity(BowlRequest req);

    @Mapping(target = "orderId", source = "order.id")
    @Mapping(target = "templateId", source = "template.id")
    @Mapping(target = "template", source = "template")
    @Mapping(target = "items", ignore = true)
    public abstract BowlResponse toResponse(Bowl entity);

    @AfterMapping
    protected void mapItems(@MappingTarget BowlResponse response, Bowl entity) {
        // Only map items if they are initialized (fetched from database)
        if (entity.getItems() != null && org.hibernate.Hibernate.isInitialized(entity.getItems())) {
            response.setItems(
                entity.getItems().stream()
                    .map(bowlItemMapper::toResponse)
                    .collect(java.util.stream.Collectors.toList())
            );
        } else {
            response.setItems(null);
        }
    }
}
