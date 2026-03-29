CREATE TABLE IF NOT EXISTS PRODUCTS (
    id VARCHAR(225) PRIMARY KEY,
    name VARCHAR(225),
    description VARCHAR(500),
    page_link VARCHAR(225),
    login_username VARCHAR(225),
    login_password VARCHAR(225),
    amount DECIMAL(19, 4),
    currency VARCHAR(10),
    other_details VARCHAR(225)
);
