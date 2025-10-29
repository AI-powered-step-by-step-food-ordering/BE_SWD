package com.officefood.healthy_food_api.mapper;

import com.officefood.healthy_food_api.dto.request.PaymentTransactionRequest;
import com.officefood.healthy_food_api.dto.response.PaymentTransactionResponse;
import com.officefood.healthy_food_api.model.*;
import org.mapstruct.*;

@Mapper(config = GlobalMapperConfig.class, imports = { com.officefood.healthy_food_api.model.enums.PaymentMethod.class, com.officefood.healthy_food_api.model.enums.PaymentStatus.class })
public interface PaymentTransactionMapper {
    @IgnoreBaseEntityFields
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", expression = "java(com.officefood.healthy_food_api.mapper.helpers.OrderMapperHelper.order(req.getOrderId()))")
    @Mapping(target = "method", expression = "java(PaymentMethod.valueOf(req.getMethod()))")
    @Mapping(target = "status", expression = "java(PaymentStatus.valueOf(req.getStatus()))")
    PaymentTransaction toEntity(PaymentTransactionRequest req);

    @Mapping(target = "orderId", source = "order.id")
    PaymentTransactionResponse toResponse(PaymentTransaction entity);
}
