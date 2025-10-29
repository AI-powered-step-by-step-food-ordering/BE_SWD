package com.officefood.healthy_food_api.model.enums;

/**
 * Loại ràng buộc giữa các ingredients
 */
public enum RestrictionType {
    EXCLUDE,    // Không được chọn cùng nhau (ví dụ: cơm chiên + cá)
    REQUIRE,    // Bắt buộc phải có cùng nhau (ví dụ: sushi + wasabi)
    RECOMMEND   // Khuyến nghị nên có cùng nhau
}
