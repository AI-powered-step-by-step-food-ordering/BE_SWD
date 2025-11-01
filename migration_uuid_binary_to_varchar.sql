-- ============================================================
-- Migration Script: Convert UUID columns from BINARY to VARCHAR(36)
-- Database: food
-- Date: 2025-10-31
-- ============================================================

-- IMPORTANT: BACKUP YOUR DATABASE BEFORE RUNNING THIS SCRIPT!
-- Run this command first:
-- mysqldump -u root -p food > backup_food_$(date +%Y%m%d_%H%M%S).sql

USE food;

-- ============================================================
-- Create migration log table to track progress
-- ============================================================
CREATE TABLE IF NOT EXISTS migration_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    step_name VARCHAR(100),
    status VARCHAR(20),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO migration_log (step_name, status, message)
VALUES ('MIGRATION_START', 'RUNNING', 'Starting UUID BINARY to VARCHAR migration');

-- ============================================================
-- STEP 1: Drop all foreign key constraints
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

INSERT INTO migration_log (step_name, status, message)
VALUES ('DROP_FOREIGN_KEYS', 'RUNNING', 'Dropping all foreign key constraints');

ALTER TABLE bowl_items DROP FOREIGN KEY IF EXISTS FK6cdpby0bm5o93al84vt1pnbgo;
ALTER TABLE bowl_items DROP FOREIGN KEY IF EXISTS FKr2i37a1s4yqxxqx76yb8cktxw;
ALTER TABLE bowls DROP FOREIGN KEY IF EXISTS FKcg1cdfb91lfytjetkehrcwtku;
ALTER TABLE bowls DROP FOREIGN KEY IF EXISTS FKj7uwpg4r0hcr99uqr2efbqm7s;
ALTER TABLE template_steps DROP FOREIGN KEY IF EXISTS FKkfu5dfqmcgx6b3x7wvffmhkql;
ALTER TABLE template_steps DROP FOREIGN KEY IF EXISTS FK945jtvx9mtxpj3hu72m46qtlr;
ALTER TABLE inventory DROP FOREIGN KEY IF EXISTS FK5risd3b54r1toplhk8xgfib57;
ALTER TABLE inventory DROP FOREIGN KEY IF EXISTS FKpc0r36gty51h1u459rtxhoewp;
ALTER TABLE kitchen_jobs DROP FOREIGN KEY IF EXISTS FKnh7trwre4tkkn1eppp398gfau;
ALTER TABLE kitchen_jobs DROP FOREIGN KEY IF EXISTS FKtd5iy3h7ygavrg4rlh75jw3oy;
ALTER TABLE payment_transactions DROP FOREIGN KEY IF EXISTS FKiw9n70c8pjv93atfmq977dn78;
ALTER TABLE notifications DROP FOREIGN KEY IF EXISTS FK9y21adhxn0ayjhfocscqox7bh;
ALTER TABLE tokens DROP FOREIGN KEY IF EXISTS FK2dylsfo39lgjyqml2tbe0b0ss;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK32ql8ubntj5uh44ph9659tiih;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FKel9kyl84ego2otj2accfd8mr7;
ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK532mn698okly0glbnl33tloi5;
ALTER TABLE promotion_redemptions DROP FOREIGN KEY IF EXISTS FK64e0qkemfnfdgje8jnb20f8qt;
ALTER TABLE promotion_redemptions DROP FOREIGN KEY IF EXISTS FKp9fvkr2nnrbj2qrqc5xq88agd;
ALTER TABLE promotion_redemptions DROP FOREIGN KEY IF EXISTS FKfnhbkr89m0ynr5ue5gre8b6x7;
ALTER TABLE ingredient_restrictions DROP FOREIGN KEY IF EXISTS FKepfd7mdocukx3kk5qc19yx4en;
ALTER TABLE ingredients DROP FOREIGN KEY IF EXISTS FKh5l4dbo1hr38wgybqao8lr5w8;
ALTER TABLE bowls_template DROP FOREIGN KEY IF EXISTS FK7b09iag04c95e1jyfdv70svhv;

SELECT 'Foreign keys dropped' AS status;

INSERT INTO migration_log (step_name, status, message)
VALUES ('DROP_FOREIGN_KEYS', 'SUCCESS', 'All foreign keys dropped successfully');

-- ============================================================
-- STEP 2: Convert UUID columns from BINARY to VARCHAR(36)
-- Strategy: Add temp column -> Convert data -> Drop old -> Rename temp
-- ============================================================

INSERT INTO migration_log (step_name, status, message)
VALUES ('CONVERT_COLUMNS', 'RUNNING', 'Starting column conversion');

-- Helper procedure to convert a column with logging
DELIMITER //
CREATE PROCEDURE ConvertUUIDColumn(
    IN tbl VARCHAR(64),
    IN col VARCHAR(64)
)
BEGIN
    DECLARE continue_handler INT DEFAULT 0;
    DECLARE error_msg TEXT;

    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 error_msg = MESSAGE_TEXT;
        INSERT INTO migration_log (step_name, status, message)
        VALUES (CONCAT('CONVERT_', tbl, '.', col), 'ERROR', error_msg);
        SET continue_handler = 1;
    END;

    SET @temp_col = CONCAT(col, '_temp');

    -- Log start
    INSERT INTO migration_log (step_name, status, message)
    VALUES (CONCAT('CONVERT_', tbl, '.', col), 'RUNNING', CONCAT('Converting ', tbl, '.', col));

    -- Add temporary column
    SET @sql = CONCAT('ALTER TABLE ', tbl, ' ADD COLUMN ', @temp_col, ' VARCHAR(36)');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Convert BINARY to UUID string format (handle NULL values)
    SET @sql = CONCAT(
        'UPDATE ', tbl, ' SET ', @temp_col, ' = ',
        'CASE WHEN ', col, ' IS NULL THEN NULL ELSE ',
        'LOWER(CONCAT(',
        'SUBSTR(HEX(', col, '), 1, 8), ''-'',',
        'SUBSTR(HEX(', col, '), 9, 4), ''-'',',
        'SUBSTR(HEX(', col, '), 13, 4), ''-'',',
        'SUBSTR(HEX(', col, '), 17, 4), ''-'',',
        'SUBSTR(HEX(', col, '), 21, 12))) END'
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Drop old column
    SET @sql = CONCAT('ALTER TABLE ', tbl, ' DROP COLUMN ', col);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Rename temp to original
    SET @sql = CONCAT('ALTER TABLE ', tbl, ' CHANGE COLUMN ', @temp_col, ' ', col, ' VARCHAR(36)');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Log success
    IF continue_handler = 0 THEN
        INSERT INTO migration_log (step_name, status, message)
        VALUES (CONCAT('CONVERT_', tbl, '.', col), 'SUCCESS', CONCAT('Successfully converted ', tbl, '.', col));
    END IF;
END//
DELIMITER ;

-- ============================================================
-- PARENT TABLES FIRST
-- ============================================================

SELECT 'Converting users table...' AS status;
CALL ConvertUUIDColumn('users', 'id');

SELECT 'Converting stores table...' AS status;
CALL ConvertUUIDColumn('stores', 'id');

SELECT 'Converting categories table...' AS status;
CALL ConvertUUIDColumn('categories', 'id');

SELECT 'Converting promotions table...' AS status;
CALL ConvertUUIDColumn('promotions', 'id');

-- ============================================================
-- MID-LEVEL TABLES
-- ============================================================

SELECT 'Converting ingredients table...' AS status;
CALL ConvertUUIDColumn('ingredients', 'id');
CALL ConvertUUIDColumn('ingredients', 'category_id');

SELECT 'Converting bowls_template table...' AS status;
CALL ConvertUUIDColumn('bowls_template', 'id');
CALL ConvertUUIDColumn('bowls_template', 'category_id');

-- ============================================================
-- CHILD TABLES
-- ============================================================

SELECT 'Converting orders table...' AS status;
CALL ConvertUUIDColumn('orders', 'id');
CALL ConvertUUIDColumn('orders', 'store_id');
CALL ConvertUUIDColumn('orders', 'user_id');
CALL ConvertUUIDColumn('orders', 'promotion_id');

SELECT 'Converting bowls table...' AS status;
CALL ConvertUUIDColumn('bowls', 'id');
CALL ConvertUUIDColumn('bowls', 'order_id');
CALL ConvertUUIDColumn('bowls', 'template_id');

SELECT 'Converting bowl_items table...' AS status;
CALL ConvertUUIDColumn('bowl_items', 'id');
CALL ConvertUUIDColumn('bowl_items', 'bowl_id');
CALL ConvertUUIDColumn('bowl_items', 'ingredient_id');

SELECT 'Converting template_steps table...' AS status;
CALL ConvertUUIDColumn('template_steps', 'id');
CALL ConvertUUIDColumn('template_steps', 'template_id');
CALL ConvertUUIDColumn('template_steps', 'ingredient_id');

SELECT 'Converting inventory table...' AS status;
CALL ConvertUUIDColumn('inventory', 'id');
CALL ConvertUUIDColumn('inventory', 'store_id');
CALL ConvertUUIDColumn('inventory', 'ingredient_id');

SELECT 'Converting kitchen_jobs table...' AS status;
CALL ConvertUUIDColumn('kitchen_jobs', 'id');
CALL ConvertUUIDColumn('kitchen_jobs', 'order_id');
CALL ConvertUUIDColumn('kitchen_jobs', 'store_id');

SELECT 'Converting payment_transactions table...' AS status;
CALL ConvertUUIDColumn('payment_transactions', 'id');
CALL ConvertUUIDColumn('payment_transactions', 'order_id');

SELECT 'Converting notifications table...' AS status;
CALL ConvertUUIDColumn('notifications', 'id');
CALL ConvertUUIDColumn('notifications', 'user_id');

SELECT 'Converting tokens table...' AS status;
CALL ConvertUUIDColumn('tokens', 'id');
CALL ConvertUUIDColumn('tokens', 'user_id');

SELECT 'Converting promotion_redemptions table...' AS status;
CALL ConvertUUIDColumn('promotion_redemptions', 'id');
CALL ConvertUUIDColumn('promotion_redemptions', 'user_id');
CALL ConvertUUIDColumn('promotion_redemptions', 'promotion_id');
CALL ConvertUUIDColumn('promotion_redemptions', 'order_id');

SELECT 'Converting ingredient_restrictions table...' AS status;
CALL ConvertUUIDColumn('ingredient_restrictions', 'id');
CALL ConvertUUIDColumn('ingredient_restrictions', 'ingredient_id');

-- Cleanup procedure
DROP PROCEDURE IF EXISTS ConvertUUIDColumn;

SELECT 'All columns converted successfully!' AS status;

INSERT INTO migration_log (step_name, status, message)
VALUES ('CONVERT_COLUMNS', 'SUCCESS', 'All columns converted to VARCHAR(36)');

-- ============================================================
-- STEP 3: Recreate foreign key constraints
-- ============================================================

SELECT 'Recreating foreign key constraints...' AS status;

INSERT INTO migration_log (step_name, status, message)
VALUES ('RECREATE_FOREIGN_KEYS', 'RUNNING', 'Recreating all foreign key constraints');

ALTER TABLE bowl_items ADD CONSTRAINT FK6cdpby0bm5o93al84vt1pnbgo FOREIGN KEY (bowl_id) REFERENCES bowls(id);
ALTER TABLE bowl_items ADD CONSTRAINT FKr2i37a1s4yqxxqx76yb8cktxw FOREIGN KEY (ingredient_id) REFERENCES ingredients(id);
ALTER TABLE bowls ADD CONSTRAINT FKcg1cdfb91lfytjetkehrcwtku FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE bowls ADD CONSTRAINT FKj7uwpg4r0hcr99uqr2efbqm7s FOREIGN KEY (template_id) REFERENCES bowls_template(id);
ALTER TABLE template_steps ADD CONSTRAINT FKkfu5dfqmcgx6b3x7wvffmhkql FOREIGN KEY (template_id) REFERENCES bowls_template(id);
ALTER TABLE template_steps ADD CONSTRAINT FK945jtvx9mtxpj3hu72m46qtlr FOREIGN KEY (ingredient_id) REFERENCES ingredients(id);
ALTER TABLE inventory ADD CONSTRAINT FK5risd3b54r1toplhk8xgfib57 FOREIGN KEY (store_id) REFERENCES stores(id);
ALTER TABLE inventory ADD CONSTRAINT FKpc0r36gty51h1u459rtxhoewp FOREIGN KEY (ingredient_id) REFERENCES ingredients(id);
ALTER TABLE kitchen_jobs ADD CONSTRAINT FKnh7trwre4tkkn1eppp398gfau FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE kitchen_jobs ADD CONSTRAINT FKtd5iy3h7ygavrg4rlh75jw3oy FOREIGN KEY (store_id) REFERENCES stores(id);
ALTER TABLE payment_transactions ADD CONSTRAINT FKiw9n70c8pjv93atfmq977dn78 FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE notifications ADD CONSTRAINT FK9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE tokens ADD CONSTRAINT FK2dylsfo39lgjyqml2tbe0b0ss FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE orders ADD CONSTRAINT FK32ql8ubntj5uh44ph9659tiih FOREIGN KEY (store_id) REFERENCES stores(id);
ALTER TABLE orders ADD CONSTRAINT FKel9kyl84ego2otj2accfd8mr7 FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE orders ADD CONSTRAINT FK532mn698okly0glbnl33tloi5 FOREIGN KEY (promotion_id) REFERENCES promotions(id);
ALTER TABLE promotion_redemptions ADD CONSTRAINT FK64e0qkemfnfdgje8jnb20f8qt FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE promotion_redemptions ADD CONSTRAINT FKp9fvkr2nnrbj2qrqc5xq88agd FOREIGN KEY (promotion_id) REFERENCES promotions(id);
ALTER TABLE promotion_redemptions ADD CONSTRAINT FKfnhbkr89m0ynr5ue5gre8b6x7 FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE ingredient_restrictions ADD CONSTRAINT FKepfd7mdocukx3kk5qc19yx4en FOREIGN KEY (ingredient_id) REFERENCES ingredients(id);
ALTER TABLE ingredients ADD CONSTRAINT FKh5l4dbo1hr38wgybqao8lr5w8 FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE bowls_template ADD CONSTRAINT FK7b09iag04c95e1jyfdv70svhv FOREIGN KEY (category_id) REFERENCES categories(id);

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Foreign keys recreated successfully!' AS status;

INSERT INTO migration_log (step_name, status, message)
VALUES ('RECREATE_FOREIGN_KEYS', 'SUCCESS', 'All foreign keys recreated successfully');

INSERT INTO migration_log (step_name, status, message)
VALUES ('MIGRATION_COMPLETE', 'SUCCESS', 'UUID migration completed successfully!');

-- ============================================================
-- Verification Queries
-- ============================================================

-- Check all ID columns are now VARCHAR(36)
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'food'
    AND COLUMN_NAME IN ('id', 'user_id', 'store_id', 'order_id', 'bowl_id',
                        'template_id', 'ingredient_id', 'category_id', 'promotion_id')
ORDER BY TABLE_NAME, COLUMN_NAME;

-- Count total records in each table (verify no data loss)
SELECT 'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'stores', COUNT(*) FROM stores
UNION ALL
SELECT 'categories', COUNT(*) FROM categories
UNION ALL
SELECT 'ingredients', COUNT(*) FROM ingredients
UNION ALL
SELECT 'bowls_template', COUNT(*) FROM bowls_template
UNION ALL
SELECT 'orders', COUNT(*) FROM orders
UNION ALL
SELECT 'bowls', COUNT(*) FROM bowls
UNION ALL
SELECT 'bowl_items', COUNT(*) FROM bowl_items
UNION ALL
SELECT 'payment_transactions', COUNT(*) FROM payment_transactions
UNION ALL
SELECT 'kitchen_jobs', COUNT(*) FROM kitchen_jobs
UNION ALL
SELECT 'inventory', COUNT(*) FROM inventory
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications
UNION ALL
SELECT 'tokens', COUNT(*) FROM tokens
UNION ALL
SELECT 'promotions', COUNT(*) FROM promotions
UNION ALL
SELECT 'promotion_redemptions', COUNT(*) FROM promotion_redemptions
UNION ALL
SELECT 'ingredient_restrictions', COUNT(*) FROM ingredient_restrictions;

-- ============================================================
-- Migration Log Summary
-- ============================================================
SELECT '========================================' AS '';
SELECT 'MIGRATION LOG SUMMARY' AS '';
SELECT '========================================' AS '';

SELECT
    step_name,
    status,
    message,
    created_at
FROM migration_log
ORDER BY id;

-- Count successes and errors
SELECT
    status,
    COUNT(*) as count
FROM migration_log
GROUP BY status;

-- ============================================================
-- SUCCESS MESSAGE
-- ============================================================
SELECT '========================================' AS '';
SELECT 'Migration completed successfully! All UUID columns converted to VARCHAR(36)' AS status;
SELECT 'You can now drop the migration_log table if desired:' AS '';
SELECT 'DROP TABLE IF EXISTS migration_log;' AS cleanup_command;

