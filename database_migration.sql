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
