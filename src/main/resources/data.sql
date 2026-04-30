-- Seed data for dev profile (H2)
-- Passwords are BCrypt-hashed: "Password1"

-- Admin user
INSERT INTO users (name, email, password, role, active, email_verified, created_at, updated_at) VALUES
('Admin', 'admin@globalpass.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true, true, NOW(), NOW());

-- Regular user
INSERT INTO users (name, email, password, role, active, email_verified, created_at, updated_at) VALUES
('Test User', 'user@globalpass.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true, true, NOW(), NOW());
