-- =============================================
-- GlobalPass Database Schema (PostgreSQL / Supabase)
-- Matches current Java entities
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    password            VARCHAR(255),
    auth_provider       VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    role                VARCHAR(10)  NOT NULL DEFAULT 'USER',
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    reset_token         VARCHAR(255),
    reset_token_expiry  TIMESTAMP,
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token  VARCHAR(255),
    password_changed_at TIMESTAMP,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bookings (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id),
    name               VARCHAR(255),
    description         TEXT,
    page_link          VARCHAR(500),
    login_username     VARCHAR(255),
    login_password     VARCHAR(255),
    amount             DOUBLE PRECISION,
    currency           VARCHAR(10),
    other_details      TEXT,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS payments (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id),
    user_name          VARCHAR(255),
    user_email         VARCHAR(255),
    booking_id         VARCHAR(255) NOT NULL,
    booking_name       VARCHAR(255),
    amount             DOUBLE PRECISION,
    file_name          VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type       VARCHAR(255) NOT NULL,
    file_size          BIGINT       NOT NULL,
    note               TEXT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    admin_note         TEXT,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_bookings_user_id ON bookings(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at DESC);
