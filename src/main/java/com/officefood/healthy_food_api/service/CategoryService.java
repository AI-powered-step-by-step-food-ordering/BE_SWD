package com.officefood.healthy_food_api.service;

import com.officefood.healthy_food_api.dto.request.CategorySearchRequest;
import com.officefood.healthy_food_api.model.Category;

import java.util.List;

public interface CategoryService extends CrudService<Category> {
    // Search functionality
    List<Category> search(CategorySearchRequest searchRequest);
}
