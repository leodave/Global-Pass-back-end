-- ──────────────────────────────────────────────
-- CLEAN EXISTING DATA
-- ──────────────────────────────────────────────
DELETE FROM bookings;
DELETE FROM users;

-- ──────────────────────────────────────────────
-- SEED USERS
-- 3 LOCAL (email/password) users
-- 2 GOOGLE (OAuth2) users — no password
-- ──────────────────────────────────────────────
INSERT INTO users (id, name, email, password, auth_provider, role, active) VALUES
    (1, 'Alice Johnson',  'alice@email.com',   '$2a$10$xK5G5z5z5z5z5z5z5z5zuOQKQKQKQKQKQKQKQKQKQKQKQKQKQKQK', 'LOCAL',  'USER',  TRUE),
    (2, 'Bob Smith',      'bob@email.com',     '$2a$10$xK5G5z5z5z5z5z5z5z5zuOQKQKQKQKQKQKQKQKQKQKQKQKQKQKQK', 'LOCAL',  'USER',  TRUE),
    (3, 'Carol White',    'carol@email.com',   '$2a$10$xK5G5z5z5z5z5z5z5z5zuOQKQKQKQKQKQKQKQKQKQKQKQKQKQKQK', 'LOCAL',  'ADMIN', TRUE),
    (4, 'david@gmail.com','david@gmail.com',   NULL,                                                           'GOOGLE', 'USER',  TRUE),
    (5, 'emma@gmail.com', 'emma@gmail.com',    NULL,                                                           'GOOGLE', 'USER',  TRUE);

-- ──────────────────────────────────────────────
-- SEED BOOKINGS
-- each user has at least 1 booking
-- ──────────────────────────────────────────────
INSERT INTO bookings (id, user_id, name, description, page_link, login_username, login_password, amount, currency, other_details) VALUES

    -- Alice (user_id = 1)
    ('book-001', 1, 'Netflix Standard',     'Streaming with HD quality and 2 screens',  'https://netflix.com/login',   'alice@email.com', 'NetPass123!',   15.99, 'USD', 'Renews monthly on the 1st'),
    ('book-002', 1, 'Spotify Premium',      'Ad-free music with offline downloads',      'https://spotify.com/login',   'alice@email.com', 'SpotPass456!',   9.99, 'USD', 'Family plan - 6 accounts'),
      -- Bob (user_id = 2)
    ('book-003', 2, 'Adobe Creative Cloud', 'Full suite of Adobe design tools',          'https://adobe.com/login',     'bob@email.com',   'AdobePass789!', 54.99, 'USD', 'Annual plan, next renewal Jan 2026'),
    ('book-004', 2, 'GitHub Pro',           'Advanced GitHub features for developers',   'https://github.com/login',    'bob@email.com',   'GithubPass321!', 4.00, 'USD', 'Includes private repos and Actions minutes'),
    -- Carol (user_id = 3)
    ('book-005', 3, 'NordVPN',              'VPN service with 6 device connections',     'https://nordvpn.com/login',   'carol@email.com', 'NordPass654!',   4.99, 'USD', '2 year plan expires Dec 2026'),
    ('book-006', 3, 'Microsoft 365',        'Office apps with 1TB OneDrive storage',     'https://microsoft.com/login', 'carol@email.com', 'MSPass147!',     9.99, 'USD', 'Personal plan, 5 devices');
