-- ──────────────────────────────────────────────
-- USERS TABLE
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(225) NOT NULL,
    email VARCHAR(225) NOT NULL UNIQUE,
    password VARCHAR(225),
    auth_provider VARCHAR(50) DEFAULT 'LOCAL',
    role VARCHAR(50) DEFAULT 'USER',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- ──────────────────────────────────────────────
-- BOOKINGS TABLE
-- ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bookings (
    id VARCHAR(225) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(225),
    description VARCHAR(500),
    page_link VARCHAR(225),
    login_username VARCHAR(225),
    login_password VARCHAR(225),
    amount DECIMAL(19, 4),
    currency VARCHAR(10),
    other_details VARCHAR(225),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
