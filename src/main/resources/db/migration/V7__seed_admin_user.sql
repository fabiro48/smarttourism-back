-- V7: Seed admin user
-- Default password: Admin123! (BCrypt hash)
-- This should be changed immediately after first deployment
INSERT INTO users (id, full_name, email, password, phone, document_number, role, active, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'Administrador del Sistema',
    'admin@smarttourism.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '+57 300 1234567',
    'ADMIN-001',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
