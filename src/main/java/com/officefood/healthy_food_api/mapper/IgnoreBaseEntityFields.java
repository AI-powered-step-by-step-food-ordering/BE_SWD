package com.officefood.healthy_food_api.mapper;

import org.mapstruct.Mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để ignore các trường của BaseEntity khi mapping
 * Sử dụng cho tất cả các mapper để tránh lặp lại code
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "deletedAt", ignore = true)
@Mapping(target = "isActive", ignore = true)
public @interface IgnoreBaseEntityFields {
}

