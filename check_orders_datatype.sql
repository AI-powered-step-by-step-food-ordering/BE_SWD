-- Script để kiểm tra kiểu dữ liệu của bảng orders và các bảng liên quan
-- Chạy script này TRƯỚC KHI chạy migration để verify

USE food;

-- =====================================================
-- Kiểm tra cấu trúc bảng orders
-- =====================================================
SHOW CREATE TABLE orders;

-- Hoặc dùng query này để xem chi tiết
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE,
    COLUMN_KEY,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'food'
AND TABLE_NAME = 'orders'
AND COLUMN_NAME = 'id';

-- =====================================================
-- Kiểm tra các foreign keys hiện có trỏ đến orders
-- =====================================================
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE REFERENCED_TABLE_SCHEMA = 'food'
AND REFERENCED_TABLE_NAME = 'orders'
AND REFERENCED_COLUMN_NAME = 'id';

-- =====================================================
-- Kiểm tra kiểu dữ liệu của các cột tham chiếu đến orders.id
-- =====================================================
SELECT
    t.TABLE_NAME,
    c.COLUMN_NAME,
    c.COLUMN_TYPE,
    c.DATA_TYPE,
    k.REFERENCED_TABLE_NAME,
    k.REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE k
JOIN INFORMATION_SCHEMA.COLUMNS c
    ON k.TABLE_SCHEMA = c.TABLE_SCHEMA
    AND k.TABLE_NAME = c.TABLE_NAME
    AND k.COLUMN_NAME = c.COLUMN_NAME
JOIN INFORMATION_SCHEMA.TABLES t
    ON k.TABLE_SCHEMA = t.TABLE_SCHEMA
    AND k.TABLE_NAME = t.TABLE_NAME
WHERE k.REFERENCED_TABLE_SCHEMA = 'food'
AND k.REFERENCED_TABLE_NAME = 'orders'
AND k.REFERENCED_COLUMN_NAME = 'id';

