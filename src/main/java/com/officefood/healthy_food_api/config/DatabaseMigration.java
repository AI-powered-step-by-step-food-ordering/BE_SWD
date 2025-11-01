package com.officefood.healthy_food_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        migratePasswordColumn();
        migrateUUIDColumnsToVarchar();
    }

    private void migratePasswordColumn() {
        try {
            String checkColumnSql = """
                SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = 'food' 
                AND TABLE_NAME = 'users' 
                AND COLUMN_NAME = 'password'
                """;

            Integer columnExists = jdbcTemplate.queryForObject(checkColumnSql, Integer.class);

            if (columnExists != null && columnExists > 0) {
                System.out.println("Dropping old password column from users table...");
                jdbcTemplate.execute("ALTER TABLE users DROP COLUMN password");
                System.out.println("Old password column dropped successfully!");
            } else {
                System.out.println("Old password column not found, no migration needed.");
            }
        } catch (Exception e) {
            System.err.println("Error during password column migration: " + e.getMessage());
        }
    }

    private void migrateUUIDColumnsToVarchar() {
        try {
            System.out.println("Starting String columns migration from BINARY to VARCHAR(36)...");

            // Check if migration is needed by checking users.id column type
            String checkIdTypeSql = """
                SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = 'food'
                AND TABLE_NAME = 'users'
                AND COLUMN_NAME = 'id'
                """;

            String dataType = jdbcTemplate.queryForObject(checkIdTypeSql, String.class);

            if ("binary".equalsIgnoreCase(dataType) || "varbinary".equalsIgnoreCase(dataType)) {
                System.out.println("Binary ID columns detected. Starting conversion...");
                System.out.println("âš ï¸  WARNING: This migration will drop all foreign keys and recreate them!");

                // Step 1: Drop all foreign key constraints
                System.out.println("Step 1: Dropping foreign key constraints...");
                dropAllForeignKeys();

                // Step 2: Convert all columns (parent tables first, then children)
                System.out.println("Step 2: Converting String columns...");

                // Parent tables first (no dependencies)
                convertTableWithUuidConversion("users", "id");
                convertTableWithUuidConversion("stores", "id");
                convertTableWithUuidConversion("categories", "id");
                convertTableWithUuidConversion("promotions", "id");

                // Mid-level tables
                convertTableWithUuidConversion("ingredients", "id", "category_id");
                convertTableWithUuidConversion("bowls_template", "id", "category_id");

                // Child tables
                convertTableWithUuidConversion("orders", "id", "store_id", "user_id", "promotion_id");
                convertTableWithUuidConversion("bowls", "id", "order_id", "template_id");
                convertTableWithUuidConversion("bowl_items", "id", "bowl_id", "ingredient_id");
                convertTableWithUuidConversion("template_steps", "id", "template_id", "ingredient_id");
                convertTableWithUuidConversion("inventory", "id", "store_id", "ingredient_id");
                convertTableWithUuidConversion("kitchen_jobs", "id", "order_id", "store_id");
                convertTableWithUuidConversion("payment_transactions", "id", "order_id");
                convertTableWithUuidConversion("notifications", "id", "user_id");
                convertTableWithUuidConversion("tokens", "id", "user_id");
                convertTableWithUuidConversion("promotion_redemptions", "id", "user_id", "promotion_id", "order_id");
                convertTableWithUuidConversion("ingredient_restrictions", "id", "ingredient_id");

                // Step 3: Recreate foreign key constraints
                System.out.println("Step 3: Recreating foreign key constraints...");
                recreateAllForeignKeys();

                System.out.println("âœ… String migration completed successfully!");
            } else {
                System.out.println("ID columns are already VARCHAR, no migration needed.");
            }
        } catch (Exception e) {
            System.err.println("âŒ Error during String migration: " + e.getMessage());
            System.err.println("âš ï¸  Database may be in inconsistent state. Please restore from backup!");
        }
    }

    private void dropAllForeignKeys() {
        String[] dropFkStatements = {
            "ALTER TABLE bowl_items DROP FOREIGN KEY IF EXISTS FK6cdpby0bm5o93al84vt1pnbgo",
            "ALTER TABLE bowl_items DROP FOREIGN KEY IF EXISTS FKr2i37a1s4yqxxqx76yb8cktxw",
            "ALTER TABLE bowls DROP FOREIGN KEY IF EXISTS FKcg1cdfb91lfytjetkehrcwtku",
            "ALTER TABLE bowls DROP FOREIGN KEY IF EXISTS FKj7uwpg4r0hcr99uqr2efbqm7s",
            "ALTER TABLE template_steps DROP FOREIGN KEY IF EXISTS FKkfu5dfqmcgx6b3x7wvffmhkql",
            "ALTER TABLE template_steps DROP FOREIGN KEY IF EXISTS FK945jtvx9mtxpj3hu72m46qtlr",
            "ALTER TABLE inventory DROP FOREIGN KEY IF EXISTS FK5risd3b54r1toplhk8xgfib57",
            "ALTER TABLE inventory DROP FOREIGN KEY IF EXISTS FKpc0r36gty51h1u459rtxhoewp",
            "ALTER TABLE kitchen_jobs DROP FOREIGN KEY IF EXISTS FKnh7trwre4tkkn1eppp398gfau",
            "ALTER TABLE kitchen_jobs DROP FOREIGN KEY IF EXISTS FKtd5iy3h7ygavrg4rlh75jw3oy",
            "ALTER TABLE payment_transactions DROP FOREIGN KEY IF EXISTS FKiw9n70c8pjv93atfmq977dn78",
            "ALTER TABLE notifications DROP FOREIGN KEY IF EXISTS FK9y21adhxn0ayjhfocscqox7bh",
            "ALTER TABLE tokens DROP FOREIGN KEY IF EXISTS FK2dylsfo39lgjyqml2tbe0b0ss",
            "ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK32ql8ubntj5uh44ph9659tiih",
            "ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FKel9kyl84ego2otj2accfd8mr7",
            "ALTER TABLE orders DROP FOREIGN KEY IF EXISTS FK532mn698okly0glbnl33tloi5",
            "ALTER TABLE promotion_redemptions DROP FOREIGN KEY IF EXISTS FK64e0qkemfnfdgje8jnb20f8qt",
            "ALTER TABLE promotion_redemptions DROP FOREIGN KEY IF EXISTS FKp9fvkr2nnrbj2qrqc5xq88agd",
            "ALTER TABLE promotion_redemptions DROP FOREIGN KEY IF EXISTS FKfnhbkr89m0ynr5ue5gre8b6x7",
            "ALTER TABLE ingredient_restrictions DROP FOREIGN KEY IF EXISTS FKepfd7mdocukx3kk5qc19yx4en",
            "ALTER TABLE ingredients DROP FOREIGN KEY IF EXISTS FKh5l4dbo1hr38wgybqao8lr5w8",
            "ALTER TABLE bowls_template DROP FOREIGN KEY IF EXISTS FK7b09iag04c95e1jyfdv70svhv"
        };

        for (String sql : dropFkStatements) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                // Ignore if FK doesn't exist
            }
        }
    }

    private void convertTableWithUuidConversion(String tableName, String... columns) {
        try {
            System.out.println("  Converting table: " + tableName);

            for (String column : columns) {
                if (column != null && !column.isEmpty()) {
                    // Add a temporary column
                    String tempColumn = column + "_temp";

                    try {
                        // 1. Add temporary VARCHAR column
                        jdbcTemplate.execute(String.format(
                            "ALTER TABLE %s ADD COLUMN %s VARCHAR(36)",
                            tableName, tempColumn
                        ));

                        // 2. Convert BINARY to String string format using LOWER(HEX())
                        jdbcTemplate.execute(String.format(
                            "UPDATE %s SET %s = LOWER(CONCAT(" +
                            "SUBSTR(HEX(%s), 1, 8), '-'," +
                            "SUBSTR(HEX(%s), 9, 4), '-'," +
                            "SUBSTR(HEX(%s), 13, 4), '-'," +
                            "SUBSTR(HEX(%s), 17, 4), '-'," +
                            "SUBSTR(HEX(%s), 21, 12)))",
                            tableName, tempColumn, column, column, column, column, column
                        ));

                        // 3. Drop old BINARY column
                        jdbcTemplate.execute(String.format(
                            "ALTER TABLE %s DROP COLUMN %s",
                            tableName, column
                        ));

                        // 4. Rename temp column to original name
                        jdbcTemplate.execute(String.format(
                            "ALTER TABLE %s CHANGE COLUMN %s %s VARCHAR(36)",
                            tableName, tempColumn, column
                        ));

                        System.out.println("    âœ“ Converted column: " + column);
                    } catch (Exception e) {
                        System.err.println("    âœ— Error converting column " + column + ": " + e.getMessage());
                        // Try to cleanup temp column
                        try {
                            jdbcTemplate.execute(String.format("ALTER TABLE %s DROP COLUMN IF EXISTS %s", tableName, tempColumn));
                        } catch (Exception ex) {
                            // Ignore
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("  âœ— Error converting table " + tableName + ": " + e.getMessage());
        }
    }

    private void recreateAllForeignKeys() {
        String[] createFkStatements = {
            "ALTER TABLE bowl_items ADD CONSTRAINT FK6cdpby0bm5o93al84vt1pnbgo FOREIGN KEY (bowl_id) REFERENCES bowls(id)",
            "ALTER TABLE bowl_items ADD CONSTRAINT FKr2i37a1s4yqxxqx76yb8cktxw FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)",
            "ALTER TABLE bowls ADD CONSTRAINT FKcg1cdfb91lfytjetkehrcwtku FOREIGN KEY (order_id) REFERENCES orders(id)",
            "ALTER TABLE bowls ADD CONSTRAINT FKj7uwpg4r0hcr99uqr2efbqm7s FOREIGN KEY (template_id) REFERENCES bowls_template(id)",
            "ALTER TABLE template_steps ADD CONSTRAINT FKkfu5dfqmcgx6b3x7wvffmhkql FOREIGN KEY (template_id) REFERENCES bowls_template(id)",
            "ALTER TABLE template_steps ADD CONSTRAINT FK945jtvx9mtxpj3hu72m46qtlr FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)",
            "ALTER TABLE inventory ADD CONSTRAINT FK5risd3b54r1toplhk8xgfib57 FOREIGN KEY (store_id) REFERENCES stores(id)",
            "ALTER TABLE inventory ADD CONSTRAINT FKpc0r36gty51h1u459rtxhoewp FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)",
            "ALTER TABLE kitchen_jobs ADD CONSTRAINT FKnh7trwre4tkkn1eppp398gfau FOREIGN KEY (order_id) REFERENCES orders(id)",
            "ALTER TABLE kitchen_jobs ADD CONSTRAINT FKtd5iy3h7ygavrg4rlh75jw3oy FOREIGN KEY (store_id) REFERENCES stores(id)",
            "ALTER TABLE payment_transactions ADD CONSTRAINT FKiw9n70c8pjv93atfmq977dn78 FOREIGN KEY (order_id) REFERENCES orders(id)",
            "ALTER TABLE notifications ADD CONSTRAINT FK9y21adhxn0ayjhfocscqox7bh FOREIGN KEY (user_id) REFERENCES users(id)",
            "ALTER TABLE tokens ADD CONSTRAINT FK2dylsfo39lgjyqml2tbe0b0ss FOREIGN KEY (user_id) REFERENCES users(id)",
            "ALTER TABLE orders ADD CONSTRAINT FK32ql8ubntj5uh44ph9659tiih FOREIGN KEY (store_id) REFERENCES stores(id)",
            "ALTER TABLE orders ADD CONSTRAINT FKel9kyl84ego2otj2accfd8mr7 FOREIGN KEY (user_id) REFERENCES users(id)",
            "ALTER TABLE orders ADD CONSTRAINT FK532mn698okly0glbnl33tloi5 FOREIGN KEY (promotion_id) REFERENCES promotions(id)",
            "ALTER TABLE promotion_redemptions ADD CONSTRAINT FK64e0qkemfnfdgje8jnb20f8qt FOREIGN KEY (user_id) REFERENCES users(id)",
            "ALTER TABLE promotion_redemptions ADD CONSTRAINT FKp9fvkr2nnrbj2qrqc5xq88agd FOREIGN KEY (promotion_id) REFERENCES promotions(id)",
            "ALTER TABLE promotion_redemptions ADD CONSTRAINT FKfnhbkr89m0ynr5ue5gre8b6x7 FOREIGN KEY (order_id) REFERENCES orders(id)",
            "ALTER TABLE ingredient_restrictions ADD CONSTRAINT FKepfd7mdocukx3kk5qc19yx4en FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)",
            "ALTER TABLE ingredients ADD CONSTRAINT FKh5l4dbo1hr38wgybqao8lr5w8 FOREIGN KEY (category_id) REFERENCES categories(id)",
            "ALTER TABLE bowls_template ADD CONSTRAINT FK7b09iag04c95e1jyfdv70svhv FOREIGN KEY (category_id) REFERENCES categories(id)"
        };

        for (String sql : createFkStatements) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                System.err.println("    âš  Warning: Could not recreate FK: " + e.getMessage());
            }
        }
    }
}
