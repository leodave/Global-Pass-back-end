-- Admin test user for development
-- Email: admin@globalpass.com
-- Password: Admin@2025
-- BCrypt hash generated via: bcrypt.hashpw(b'Admin@2025', bcrypt.gensalt())
INSERT INTO users (name, email, password, role, active, created_at, updated_at)
VALUES (
    'Admin',
    'admin@globalpass.com',
    '$2b$12$dO7y38Zh.AXE/3sEpgMRneIeC2wMIyHWAfkKt7QWVyxupTRX.xJLy',
    'ADMIN',
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;
