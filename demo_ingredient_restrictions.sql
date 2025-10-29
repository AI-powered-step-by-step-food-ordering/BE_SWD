-- Demo SQL script cho tính năng ingredient restrictions
-- Tạo dữ liệu mẫu để test tính năng "cơm chiên không được chọn cá, sushi không được chọn mayonaise"

-- 1. Tạo categories
INSERT INTO categories (id, name, kind, display_order, is_active) VALUES
(UNHEX('11111111111111111111111111111111'), 'Carbohydrates', 'CARB', 1, true),
(UNHEX('22222222222222222222222222222222'), 'Proteins', 'PROTEIN', 2, true),
(UNHEX('33333333333333333333333333333333'), 'Sauces', 'SAUCE', 3, true);

-- 2. Tạo ingredients
INSERT INTO ingredients (id, name, category_id, unit, unit_price) VALUES
-- Carbs
(UNHEX('AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1'), 'Cơm chiên', UNHEX('11111111111111111111111111111111'), 'portion', 25000),
(UNHEX('AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA2'), 'Sushi rice', UNHEX('11111111111111111111111111111111'), 'portion', 30000),
-- Proteins
(UNHEX('BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB1'), 'Cá hồi', UNHEX('22222222222222222222222222222222'), 'slice', 15000),
(UNHEX('BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB2'), 'Cá ngừ', UNHEX('22222222222222222222222222222222'), 'slice', 12000),
(UNHEX('BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB3'), 'Thịt gà', UNHEX('22222222222222222222222222222222'), 'piece', 10000),
-- Sauces
(UNHEX('CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC1'), 'Mayonaise', UNHEX('33333333333333333333333333333333'), 'tbsp', 2000),
(UNHEX('CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC2'), 'Soy sauce', UNHEX('33333333333333333333333333333333'), 'tbsp', 1500);

-- 3. Tạo ingredient restrictions
INSERT INTO ingredient_restrictions (id, primary_ingredient_id, restricted_ingredient_id, type, reason, is_active) VALUES
-- Cơm chiên không được có cá
(UNHEX('DDDDDDDDDDDDDDDDDDDDDDDDDDDDDD1'),
 UNHEX('AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1'), -- Cơm chiên
 UNHEX('BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB1'), -- Cá hồi
 'EXCLUDE',
 'Cơm chiên không phù hợp với cá sống',
 true),

(UNHEX('DDDDDDDDDDDDDDDDDDDDDDDDDDDDDD2'),
 UNHEX('AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1'), -- Cơm chiên
 UNHEX('BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB2'), -- Cá ngừ
 'EXCLUDE',
 'Cơm chiên không phù hợp với cá sống',
 true),

-- Sushi không được có mayonaise
(UNHEX('DDDDDDDDDDDDDDDDDDDDDDDDDDDDDD3'),
 UNHEX('AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA2'), -- Sushi rice
 UNHEX('CCCCCCCCCCCCCCCCCCCCCCCCCCCCCC1'), -- Mayonaise
 'EXCLUDE',
 'Sushi truyền thống không dùng mayonaise',
 true);

-- 4. Tạo bowl template mẫu
INSERT INTO bowls_template (id, name, description, is_active) VALUES
(UNHEX('EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE1'), 'Custom Asian Bowl', 'Tạo bowl theo sở thích với ràng buộc ingredients', true);

-- 5. Tạo template steps
INSERT INTO template_steps (id, template_id, category_id, min_items, max_items, default_qty, display_order) VALUES
(UNHEX('FFFFFFFFFFFFFFFFFFFFFFFFFFFFF1'),
 UNHEX('EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE1'), -- Custom Asian Bowl
 UNHEX('11111111111111111111111111111111'), -- Carbohydrates
 1, 1, 1.0, 1),

(UNHEX('FFFFFFFFFFFFFFFFFFFFFFFFFFFFF2'),
 UNHEX('EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE1'), -- Custom Asian Bowl
 UNHEX('22222222222222222222222222222222'), -- Proteins
 0, 2, 1.0, 2),

(UNHEX('FFFFFFFFFFFFFFFFFFFFFFFFFFFFF3'),
 UNHEX('EEEEEEEEEEEEEEEEEEEEEEEEEEEEEE1'), -- Custom Asian Bowl
 UNHEX('33333333333333333333333333333333'), -- Sauces
 0, 2, 1.0, 3);
