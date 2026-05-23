-- Seed admin user
-- Default password: Admin123! (BCrypt hash)
INSERT INTO users (id, full_name, email, password, phone, document_number, role, active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'Administrator',
    'admin@smarttourism.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '+34600000000',
    'ADMIN001',
    'ADMIN',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
