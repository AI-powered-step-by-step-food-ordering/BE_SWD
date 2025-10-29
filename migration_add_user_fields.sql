-- Migration: Add DOB, address, and phone fields to users table
-- Date: 2025-10-29
-- Description: Add additional user information fields

-- Add date_of_birth column
ALTER TABLE users
ADD COLUMN date_of_birth DATE NULL
COMMENT 'User date of birth';

-- Add address column
ALTER TABLE users
ADD COLUMN address VARCHAR(500) NULL
COMMENT 'User address';

-- Add phone column
ALTER TABLE users
ADD COLUMN phone VARCHAR(20) NULL
COMMENT 'User phone number';

-- Verify columns added
SHOW COLUMNS FROM users WHERE Field IN ('date_of_birth', 'address', 'phone');

-- Expected result: 3 rows showing the new columns

-- NOTES:
-- ✅ All new fields are nullable to not break existing records
-- ✅ Can update existing users with new information via update endpoint
-- ✅ New registrations can optionally provide these fields

-- Sample update for existing user:
-- UPDATE users SET
--   date_of_birth = '1990-01-01',
--   address = '123 Main St, City',
--   phone = '+84123456789'
-- WHERE email = 'user@example.com';

