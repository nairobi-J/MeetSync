-- MeetSync Phase 1 Access Control Migration
-- Run these SQL commands on your PostgreSQL database

-- Add new columns for access control
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- Ensure all existing users are ACTIVE (grandfathering)
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL OR status = '';
UPDATE users SET role = 'USER' WHERE role IS NULL OR role = '';

-- Set admin role for your email
UPDATE users SET role = 'ADMIN' WHERE email = 'abusayeidsawon@gmail.com';

-- Verify changes
SELECT email, status, role FROM users ORDER BY role DESC, email;

-- MeetSync Phase 2 User Deletion Migration
-- Create user_deletions table for tracking deleted accounts

CREATE TABLE IF NOT EXISTS user_deletions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_email VARCHAR(255) NOT NULL,
    deleted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reason VARCHAR(50) NOT NULL,
    deleted_by BIGINT NOT NULL,
    notes VARCHAR(500),
    google_calendar_cleanup_attempted BOOLEAN NOT NULL DEFAULT FALSE,
    google_calendar_cleanup_successful BOOLEAN NOT NULL DEFAULT FALSE
);

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_deletions_user_id ON user_deletions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_deletions_deleted_at ON user_deletions(deleted_at);
CREATE INDEX IF NOT EXISTS idx_user_deletions_deleted_by ON user_deletions(deleted_by);

-- Add foreign key constraints (optional, but recommended)
-- ALTER TABLE user_deletions ADD CONSTRAINT fk_user_deletions_user_id 
--     FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE user_deletions ADD CONSTRAINT fk_user_deletions_deleted_by 
--     FOREIGN KEY (deleted_by) REFERENCES users(id);

-- Verify table creation
SELECT table_name, column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'user_deletions' 
ORDER BY ordinal_position;
