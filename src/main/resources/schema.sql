-- =============================================
-- GlobalPass Database Schema (PostgreSQL / Supabase)
-- =============================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(10)  NOT NULL DEFAULT 'USER',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS products (
    id              VARCHAR(225) PRIMARY KEY,
    name            VARCHAR(225),
    description     VARCHAR(500),
    page_link       VARCHAR(225),
    login_username  VARCHAR(225),
    login_password  VARCHAR(225),
    amount          DECIMAL(19, 4),
    currency        VARCHAR(10),
    other_details   VARCHAR(225)
);

CREATE TABLE IF NOT EXISTS payments (
    id                 VARCHAR(255) PRIMARY KEY,
    user_id            BIGINT       NOT NULL REFERENCES users(id),
    product_id         VARCHAR(225) NOT NULL REFERENCES products(id),
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

CREATE TABLE IF NOT EXISTS contact_messages (
    id              VARCHAR(255) PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    message         VARCHAR(2000) NOT NULL,
    read            BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at DESC);
