package com.officefood.healthy_food_api.mapper.helpers;
import com.officefood.healthy_food_api.model.Category;
public final class CategoryMapperHelper {
    private CategoryMapperHelper() { }
    public static Category category(String id) {
        if (id == null) return null;
        Category x = new Category();
        x.setId(id);
        return x;
    }
}
