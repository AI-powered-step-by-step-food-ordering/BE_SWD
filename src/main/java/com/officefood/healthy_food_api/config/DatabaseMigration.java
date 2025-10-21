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
        try {
            // Check if password column exists and drop it
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
            System.err.println("Error during database migration: " + e.getMessage());
            // Don't throw exception to prevent app startup failure
        }
    }
}
