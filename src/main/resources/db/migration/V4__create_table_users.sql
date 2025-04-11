CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'user'))
);

CREATE INDEX idx_users_email ON users(email);

INSERT INTO users (name, email, password, role)
VALUES ('Admin', 'contato@simplesdental.com', '$2a$10$KMbT%5wT*R!46i@@YHqx', 'admin');