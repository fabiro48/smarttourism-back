-- V9: Seed sample data for demonstration
-- This migration populates the database with realistic tourism data from Santander, Colombia

-- ═══════════════════════════════════════════════════════════════════════════════
-- TOURISTS (password for all: Tourist123!)
-- BCrypt hash of "Tourist123!" with rounds=10
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO users (id, full_name, email, password, phone, document_number, role, active, created_at, updated_at) VALUES
('a1b2c3d4-1111-4000-8000-000000000001', 'Carlos Andrés Martínez', 'carlos.martinez@email.com', '$2b$10$uY7sFRuShe4tkzA8mtjLJuyUZKap/lvS9oJm3rUcDeec7jdImFynW', '+57 315 2345678', '1098765432', 'TOURIST', TRUE, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),
('a1b2c3d4-2222-4000-8000-000000000002', 'María Fernanda López', 'maria.lopez@email.com', '$2b$10$uY7sFRuShe4tkzA8mtjLJuyUZKap/lvS9oJm3rUcDeec7jdImFynW', '+57 320 3456789', '1087654321', 'TOURIST', TRUE, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days'),
('a1b2c3d4-3333-4000-8000-000000000003', 'Juan Pablo Rodríguez', 'juan.rodriguez@email.com', '$2b$10$uY7sFRuShe4tkzA8mtjLJuyUZKap/lvS9oJm3rUcDeec7jdImFynW', '+57 310 4567890', '1076543210', 'TOURIST', TRUE, NOW() - INTERVAL '20 days', NOW() - INTERVAL '20 days'),
('a1b2c3d4-4444-4000-8000-000000000004', 'Laura Valentina Gómez', 'laura.gomez@email.com', '$2b$10$uY7sFRuShe4tkzA8mtjLJuyUZKap/lvS9oJm3rUcDeec7jdImFynW', '+57 318 5678901', '1065432109', 'TOURIST', TRUE, NOW() - INTERVAL '15 days', NOW() - INTERVAL '15 days'),
('a1b2c3d4-5555-4000-8000-000000000005', 'Andrés Felipe Díaz', 'andres.diaz@email.com', '$2b$10$uY7sFRuShe4tkzA8mtjLJuyUZKap/lvS9oJm3rUcDeec7jdImFynW', '+57 322 6789012', '1054321098', 'TOURIST', TRUE, NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days');


-- ═══════════════════════════════════════════════════════════════════════════════
-- EXPERIENCES (10 real tourism experiences from Santander with coordinates)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO experiences (id, title, description, category, location, duration, difficulty, price, images, active, latitude, longitude, created_at, updated_at) VALUES
('b1b2c3d4-0001-4000-8000-000000000001',
 'Senderismo en el Cañón del Chicamocha',
 'Recorre uno de los cañones más profundos del mundo con vistas espectaculares. El sendero desciende 1.5 km hasta el río Chicamocha, pasando por vegetación xerofítica y formaciones rocosas milenarias. Incluye guía certificado y refrigerio.',
 'Aventura', 'Cañón del Chicamocha, Santander', 300, 'HARD', 180000,
 ARRAY['https://picsum.photos/id/29/800/600', 'https://picsum.photos/id/28/800/600'],
 TRUE, 6.8478, -73.1198, NOW() - INTERVAL '60 days', NOW() - INTERVAL '60 days'),

('b1b2c3d4-0002-4000-8000-000000000002',
 'Parapente en Mesa de Ruitoque',
 'Vuela sobre el cañón del Chicamocha en un vuelo de parapente tándem con instructor certificado. Despegue desde la Mesa de Ruitoque a 1.700 msnm con vistas panorámicas del cañón y la meseta de Bucaramanga. Vuelo de 15-25 minutos según condiciones.',
 'Aventura', 'Mesa de Ruitoque, Floridablanca', 120, 'MODERATE', 250000,
 ARRAY['https://picsum.photos/id/96/800/600', 'https://picsum.photos/id/100/800/600'],
 TRUE, 7.0456, -73.0912, NOW() - INTERVAL '55 days', NOW() - INTERVAL '55 days'),

('b1b2c3d4-0003-4000-8000-000000000003',
 'Rafting en el Río Fonce',
 'Desciende los rápidos clase III y IV del Río Fonce en San Gil. Experiencia de 10 km de recorrido con cascadas, saltos y adrenalina pura. Incluye equipo completo, guía profesional y transporte desde San Gil.',
 'Aventura', 'Río Fonce, San Gil', 180, 'HARD', 95000,
 ARRAY['https://picsum.photos/id/40/800/600', 'https://picsum.photos/id/42/800/600'],
 TRUE, 6.5553, -73.1347, NOW() - INTERVAL '50 days', NOW() - INTERVAL '50 days'),

('b1b2c3d4-0004-4000-8000-000000000004',
 'Tour Gastronómico por Bucaramanga',
 'Descubre los sabores de Santander en un recorrido por los mejores restaurantes y mercados de Bucaramanga. Prueba hormigas culonas, mute santandereano, cabro, arepa de maíz pelao y dulces típicos. Incluye 5 paradas con degustación.',
 'Gastronomía', 'Centro Histórico, Bucaramanga', 240, 'EASY', 120000,
 ARRAY['https://picsum.photos/id/292/800/600', 'https://picsum.photos/id/312/800/600'],
 TRUE, 7.1193, -73.1227, NOW() - INTERVAL '45 days', NOW() - INTERVAL '45 days'),

('b1b2c3d4-0005-4000-8000-000000000005',
 'Caminata a la Cascada de Juan Curí',
 'Caminata ecológica de dificultad moderada hasta la impresionante Cascada de Juan Curí de 180 metros de altura. El sendero atraviesa bosque húmedo tropical con avistamiento de aves y mariposas. Opción de rappel en la cascada.',
 'Ecoturismo', 'Vereda Juan Curí, San Gil', 240, 'MODERATE', 65000,
 ARRAY['https://picsum.photos/id/15/800/600', 'https://picsum.photos/id/16/800/600'],
 TRUE, 6.6012, -73.1789, NOW() - INTERVAL '40 days', NOW() - INTERVAL '40 days'),

('b1b2c3d4-0006-4000-8000-000000000006',
 'Tour Cultural por Barichara',
 'Recorre las calles empedradas del pueblo más bonito de Colombia. Visita talleres artesanales de papel de fique, la Capilla de Santa Bárbara, el Parque de las Artes y el mirador del cañón. Incluye guía cultural bilingüe.',
 'Cultural', 'Barichara, Santander', 300, 'EASY', 85000,
 ARRAY['https://picsum.photos/id/164/800/600', 'https://picsum.photos/id/177/800/600'],
 TRUE, 6.6361, -73.2256, NOW() - INTERVAL '38 days', NOW() - INTERVAL '38 days'),

('b1b2c3d4-0007-4000-8000-000000000007',
 'Espeleología en Cueva del Indio',
 'Explora las formaciones calcáreas de la Cueva del Indio en Páramo. Recorrido subterráneo de 2 horas con estalactitas, estalagmitas y ríos subterráneos. Incluye equipo de espeleología, casco con linterna y guía especializado.',
 'Aventura', 'Páramo, Santander', 180, 'HARD', 110000,
 ARRAY['https://picsum.photos/id/137/800/600', 'https://picsum.photos/id/142/800/600'],
 TRUE, 6.4523, -73.1634, NOW() - INTERVAL '35 days', NOW() - INTERVAL '35 days'),

('b1b2c3d4-0008-4000-8000-000000000008',
 'Avistamiento de Aves en Serranía de los Yariguíes',
 'Jornada de avistamiento de aves en el Parque Nacional Serranía de los Yariguíes. Más de 300 especies registradas incluyendo endémicas. Salida a las 5:00 AM para máximo avistamiento. Incluye binoculares, guía ornitólogo y desayuno.',
 'Ecoturismo', 'Serranía de los Yariguíes, San Vicente', 480, 'MODERATE', 150000,
 ARRAY['https://picsum.photos/id/13/800/600', 'https://picsum.photos/id/14/800/600'],
 TRUE, 6.8234, -73.4567, NOW() - INTERVAL '30 days', NOW() - INTERVAL '30 days'),

('b1b2c3d4-0009-4000-8000-000000000009',
 'Teleférico del Parque Nacional del Chicamocha',
 'Recorre 6.3 km en el teleférico más largo de Colombia sobre el Cañón del Chicamocha. Vistas de 360° a más de 1.000 metros de profundidad. Incluye acceso al parque temático con museo guane, parque acuático y zona de aventura.',
 'Cultural', 'Parque Nacional del Chicamocha, Aratoca', 360, 'EASY', 75000,
 ARRAY['https://picsum.photos/id/119/800/600', 'https://picsum.photos/id/120/800/600'],
 TRUE, 6.8312, -73.1145, NOW() - INTERVAL '28 days', NOW() - INTERVAL '28 days'),

('b1b2c3d4-0010-4000-8000-000000000010',
 'Cabalgata por el Camino Real Barichara-Guane',
 'Recorre a caballo el histórico Camino Real que conecta Barichara con el pueblo colonial de Guane. Sendero empedrado de 9 km con vistas al cañón del Suárez. Incluye caballo, guía, visita al museo paleontológico de Guane y almuerzo típico.',
 'Aventura', 'Camino Real, Barichara-Guane', 300, 'MODERATE', 135000,
 ARRAY['https://picsum.photos/id/55/800/600', 'https://picsum.photos/id/57/800/600'],
 TRUE, 6.6289, -73.2198, NOW() - INTERVAL '25 days', NOW() - INTERVAL '25 days');


-- ═══════════════════════════════════════════════════════════════════════════════
-- SCHEDULES (2-3 per experience)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO schedules (id, experience_id, day_of_week, start_time, end_time, available_slots, active, created_at, updated_at) VALUES
-- Senderismo Chicamocha
('c1c2c3d4-0001-4000-8000-000000000001', 'b1b2c3d4-0001-4000-8000-000000000001', 'SATURDAY', '06:00', '11:00', 15, TRUE, NOW() - INTERVAL '58 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000001', 'b1b2c3d4-0001-4000-8000-000000000001', 'SUNDAY', '06:00', '11:00', 15, TRUE, NOW() - INTERVAL '58 days', NOW()),
-- Parapente Ruitoque
('c1c2c3d4-0001-4000-8000-000000000002', 'b1b2c3d4-0002-4000-8000-000000000002', 'MONDAY', '08:00', '10:00', 8, TRUE, NOW() - INTERVAL '53 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000002', 'b1b2c3d4-0002-4000-8000-000000000002', 'WEDNESDAY', '08:00', '10:00', 8, TRUE, NOW() - INTERVAL '53 days', NOW()),
('c1c2c3d4-0003-4000-8000-000000000002', 'b1b2c3d4-0002-4000-8000-000000000002', 'SATURDAY', '07:00', '09:00', 10, TRUE, NOW() - INTERVAL '53 days', NOW()),
-- Rafting Río Fonce
('c1c2c3d4-0001-4000-8000-000000000003', 'b1b2c3d4-0003-4000-8000-000000000003', 'TUESDAY', '09:00', '12:00', 20, TRUE, NOW() - INTERVAL '48 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000003', 'b1b2c3d4-0003-4000-8000-000000000003', 'THURSDAY', '09:00', '12:00', 20, TRUE, NOW() - INTERVAL '48 days', NOW()),
('c1c2c3d4-0003-4000-8000-000000000003', 'b1b2c3d4-0003-4000-8000-000000000003', 'SATURDAY', '08:00', '11:00', 25, TRUE, NOW() - INTERVAL '48 days', NOW()),
-- Tour Gastronómico
('c1c2c3d4-0001-4000-8000-000000000004', 'b1b2c3d4-0004-4000-8000-000000000004', 'FRIDAY', '11:00', '15:00', 12, TRUE, NOW() - INTERVAL '43 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000004', 'b1b2c3d4-0004-4000-8000-000000000004', 'SATURDAY', '11:00', '15:00', 12, TRUE, NOW() - INTERVAL '43 days', NOW()),
-- Cascada Juan Curí
('c1c2c3d4-0001-4000-8000-000000000005', 'b1b2c3d4-0005-4000-8000-000000000005', 'WEDNESDAY', '07:00', '11:00', 18, TRUE, NOW() - INTERVAL '38 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000005', 'b1b2c3d4-0005-4000-8000-000000000005', 'SUNDAY', '07:00', '11:00', 18, TRUE, NOW() - INTERVAL '38 days', NOW()),
-- Tour Barichara
('c1c2c3d4-0001-4000-8000-000000000006', 'b1b2c3d4-0006-4000-8000-000000000006', 'TUESDAY', '09:00', '14:00', 20, TRUE, NOW() - INTERVAL '36 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000006', 'b1b2c3d4-0006-4000-8000-000000000006', 'SATURDAY', '09:00', '14:00', 20, TRUE, NOW() - INTERVAL '36 days', NOW()),
-- Espeleología
('c1c2c3d4-0001-4000-8000-000000000007', 'b1b2c3d4-0007-4000-8000-000000000007', 'THURSDAY', '08:00', '11:00', 10, TRUE, NOW() - INTERVAL '33 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000007', 'b1b2c3d4-0007-4000-8000-000000000007', 'SUNDAY', '08:00', '11:00', 10, TRUE, NOW() - INTERVAL '33 days', NOW()),
-- Avistamiento de Aves
('c1c2c3d4-0001-4000-8000-000000000008', 'b1b2c3d4-0008-4000-8000-000000000008', 'WEDNESDAY', '05:00', '13:00', 8, TRUE, NOW() - INTERVAL '28 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000008', 'b1b2c3d4-0008-4000-8000-000000000008', 'SATURDAY', '05:00', '13:00', 8, TRUE, NOW() - INTERVAL '28 days', NOW()),
-- Teleférico Chicamocha
('c1c2c3d4-0001-4000-8000-000000000009', 'b1b2c3d4-0009-4000-8000-000000000009', 'MONDAY', '09:00', '15:00', 50, TRUE, NOW() - INTERVAL '26 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000009', 'b1b2c3d4-0009-4000-8000-000000000009', 'FRIDAY', '09:00', '15:00', 50, TRUE, NOW() - INTERVAL '26 days', NOW()),
('c1c2c3d4-0003-4000-8000-000000000009', 'b1b2c3d4-0009-4000-8000-000000000009', 'SUNDAY', '09:00', '15:00', 50, TRUE, NOW() - INTERVAL '26 days', NOW()),
-- Cabalgata Barichara-Guane
('c1c2c3d4-0001-4000-8000-000000000010', 'b1b2c3d4-0010-4000-8000-000000000010', 'SATURDAY', '07:00', '12:00', 10, TRUE, NOW() - INTERVAL '23 days', NOW()),
('c1c2c3d4-0002-4000-8000-000000000010', 'b1b2c3d4-0010-4000-8000-000000000010', 'SUNDAY', '07:00', '12:00', 10, TRUE, NOW() - INTERVAL '23 days', NOW());


-- ═══════════════════════════════════════════════════════════════════════════════
-- RESERVATIONS (mix of statuses: CONFIRMED, CANCELLED, PENDING_PAYMENT)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO reservations (id, tourist_id, experience_id, schedule_id, reservation_date, quantity, total_amount, status, expiration_date, created_at, updated_at) VALUES
-- Carlos: 3 reservas (2 confirmed, 1 cancelled)
('d1d2d3d4-0001-4000-8000-000000000001', 'a1b2c3d4-1111-4000-8000-000000000001', 'b1b2c3d4-0001-4000-8000-000000000001', 'c1c2c3d4-0001-4000-8000-000000000001', CURRENT_DATE - 10, 2, 360000, 'CONFIRMED', NOW() - INTERVAL '9 days' + INTERVAL '15 minutes', NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),
('d1d2d3d4-0002-4000-8000-000000000001', 'a1b2c3d4-1111-4000-8000-000000000001', 'b1b2c3d4-0003-4000-8000-000000000003', 'c1c2c3d4-0001-4000-8000-000000000003', CURRENT_DATE - 5, 3, 285000, 'CONFIRMED', NOW() - INTERVAL '4 days' + INTERVAL '15 minutes', NOW() - INTERVAL '5 days', NOW() - INTERVAL '5 days'),
('d1d2d3d4-0003-4000-8000-000000000001', 'a1b2c3d4-1111-4000-8000-000000000001', 'b1b2c3d4-0006-4000-8000-000000000006', 'c1c2c3d4-0001-4000-8000-000000000006', CURRENT_DATE - 3, 1, 85000, 'CANCELLED', NOW() - INTERVAL '2 days' + INTERVAL '15 minutes', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),

-- María: 2 reservas (confirmed)
('d1d2d3d4-0001-4000-8000-000000000002', 'a1b2c3d4-2222-4000-8000-000000000002', 'b1b2c3d4-0002-4000-8000-000000000002', 'c1c2c3d4-0003-4000-8000-000000000002', CURRENT_DATE - 8, 1, 250000, 'CONFIRMED', NOW() - INTERVAL '7 days' + INTERVAL '15 minutes', NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),
('d1d2d3d4-0002-4000-8000-000000000002', 'a1b2c3d4-2222-4000-8000-000000000002', 'b1b2c3d4-0004-4000-8000-000000000004', 'c1c2c3d4-0001-4000-8000-000000000004', CURRENT_DATE - 4, 2, 240000, 'CONFIRMED', NOW() - INTERVAL '3 days' + INTERVAL '15 minutes', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days'),

-- Juan Pablo: 2 reservas (1 confirmed, 1 pending)
('d1d2d3d4-0001-4000-8000-000000000003', 'a1b2c3d4-3333-4000-8000-000000000003', 'b1b2c3d4-0005-4000-8000-000000000005', 'c1c2c3d4-0001-4000-8000-000000000005', CURRENT_DATE - 6, 4, 260000, 'CONFIRMED', NOW() - INTERVAL '5 days' + INTERVAL '15 minutes', NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days'),
('d1d2d3d4-0002-4000-8000-000000000003', 'a1b2c3d4-3333-4000-8000-000000000003', 'b1b2c3d4-0009-4000-8000-000000000009', 'c1c2c3d4-0001-4000-8000-000000000009', CURRENT_DATE + 5, 2, 150000, 'PENDING_PAYMENT', NOW() + INTERVAL '15 minutes', NOW(), NOW()),

-- Laura: 3 reservas (all confirmed)
('d1d2d3d4-0001-4000-8000-000000000004', 'a1b2c3d4-4444-4000-8000-000000000004', 'b1b2c3d4-0007-4000-8000-000000000007', 'c1c2c3d4-0001-4000-8000-000000000007', CURRENT_DATE - 12, 1, 110000, 'CONFIRMED', NOW() - INTERVAL '11 days' + INTERVAL '15 minutes', NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),
('d1d2d3d4-0002-4000-8000-000000000004', 'a1b2c3d4-4444-4000-8000-000000000004', 'b1b2c3d4-0008-4000-8000-000000000008', 'c1c2c3d4-0001-4000-8000-000000000008', CURRENT_DATE - 7, 2, 300000, 'CONFIRMED', NOW() - INTERVAL '6 days' + INTERVAL '15 minutes', NOW() - INTERVAL '7 days', NOW() - INTERVAL '7 days'),
('d1d2d3d4-0003-4000-8000-000000000004', 'a1b2c3d4-4444-4000-8000-000000000004', 'b1b2c3d4-0010-4000-8000-000000000010', 'c1c2c3d4-0001-4000-8000-000000000010', CURRENT_DATE - 2, 2, 270000, 'CONFIRMED', NOW() - INTERVAL '1 day' + INTERVAL '15 minutes', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- Andrés: 2 reservas (1 confirmed, 1 cancelled)
('d1d2d3d4-0001-4000-8000-000000000005', 'a1b2c3d4-5555-4000-8000-000000000005', 'b1b2c3d4-0009-4000-8000-000000000009', 'c1c2c3d4-0003-4000-8000-000000000009', CURRENT_DATE - 9, 3, 225000, 'CONFIRMED', NOW() - INTERVAL '8 days' + INTERVAL '15 minutes', NOW() - INTERVAL '9 days', NOW() - INTERVAL '9 days'),
('d1d2d3d4-0002-4000-8000-000000000005', 'a1b2c3d4-5555-4000-8000-000000000005', 'b1b2c3d4-0002-4000-8000-000000000002', 'c1c2c3d4-0001-4000-8000-000000000002', CURRENT_DATE - 1, 1, 250000, 'CANCELLED', NOW() + INTERVAL '15 minutes', NOW() - INTERVAL '1 day', NOW());


-- ═══════════════════════════════════════════════════════════════════════════════
-- PAYMENTS (for CONFIRMED reservations)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO payments (id, reservation_id, payment_date, transaction_reference, status, amount, created_at) VALUES
-- Carlos payments
('e1e2e3e4-0001-4000-8000-000000000001', 'd1d2d3d4-0001-4000-8000-000000000001', NOW() - INTERVAL '10 days', 'TXN-2024-CHIC-001', 'APPROVED', 360000, NOW() - INTERVAL '10 days'),
('e1e2e3e4-0002-4000-8000-000000000001', 'd1d2d3d4-0002-4000-8000-000000000001', NOW() - INTERVAL '5 days', 'TXN-2024-RAFT-002', 'APPROVED', 285000, NOW() - INTERVAL '5 days'),
-- María payments
('e1e2e3e4-0001-4000-8000-000000000002', 'd1d2d3d4-0001-4000-8000-000000000002', NOW() - INTERVAL '8 days', 'TXN-2024-PARA-003', 'APPROVED', 250000, NOW() - INTERVAL '8 days'),
('e1e2e3e4-0002-4000-8000-000000000002', 'd1d2d3d4-0002-4000-8000-000000000002', NOW() - INTERVAL '4 days', 'TXN-2024-GAST-004', 'APPROVED', 240000, NOW() - INTERVAL '4 days'),
-- Juan Pablo payments
('e1e2e3e4-0001-4000-8000-000000000003', 'd1d2d3d4-0001-4000-8000-000000000003', NOW() - INTERVAL '6 days', 'TXN-2024-CASC-005', 'APPROVED', 260000, NOW() - INTERVAL '6 days'),
-- Laura payments
('e1e2e3e4-0001-4000-8000-000000000004', 'd1d2d3d4-0001-4000-8000-000000000004', NOW() - INTERVAL '12 days', 'TXN-2024-ESPE-006', 'APPROVED', 110000, NOW() - INTERVAL '12 days'),
('e1e2e3e4-0002-4000-8000-000000000004', 'd1d2d3d4-0002-4000-8000-000000000004', NOW() - INTERVAL '7 days', 'TXN-2024-AVES-007', 'APPROVED', 300000, NOW() - INTERVAL '7 days'),
('e1e2e3e4-0003-4000-8000-000000000004', 'd1d2d3d4-0003-4000-8000-000000000004', NOW() - INTERVAL '2 days', 'TXN-2024-CABA-008', 'APPROVED', 270000, NOW() - INTERVAL '2 days'),
-- Andrés payments
('e1e2e3e4-0001-4000-8000-000000000005', 'd1d2d3d4-0001-4000-8000-000000000005', NOW() - INTERVAL '9 days', 'TXN-2024-TELE-009', 'APPROVED', 225000, NOW() - INTERVAL '9 days'),
-- A rejected payment for variety
('e1e2e3e4-0002-4000-8000-000000000005', 'd1d2d3d4-0002-4000-8000-000000000005', NOW() - INTERVAL '1 day', 'TXN-2024-PARA-010', 'REJECTED', 250000, NOW() - INTERVAL '1 day');

-- ═══════════════════════════════════════════════════════════════════════════════
-- REVIEWS (for confirmed experiences that already happened)
-- ═══════════════════════════════════════════════════════════════════════════════

INSERT INTO reviews (id, tourist_id, experience_id, rating, comment, created_at) VALUES
-- Carlos reviews
('f1f2f3f4-0001-4000-8000-000000000001', 'a1b2c3d4-1111-4000-8000-000000000001', 'b1b2c3d4-0001-4000-8000-000000000001', 5, 'Increíble experiencia. El cañón es impresionante y el guía muy profesional. Recomendado para quienes buscan aventura real.', NOW() - INTERVAL '8 days'),
('f1f2f3f4-0002-4000-8000-000000000001', 'a1b2c3d4-1111-4000-8000-000000000001', 'b1b2c3d4-0003-4000-8000-000000000003', 4, 'El rafting estuvo genial, los rápidos clase IV son emocionantes. Solo le quito una estrella porque el transporte tardó un poco.', NOW() - INTERVAL '3 days'),

-- María reviews
('f1f2f3f4-0001-4000-8000-000000000002', 'a1b2c3d4-2222-4000-8000-000000000002', 'b1b2c3d4-0002-4000-8000-000000000002', 5, 'Volar sobre el cañón fue la mejor experiencia de mi vida. El instructor muy profesional y las vistas son de otro mundo.', NOW() - INTERVAL '6 days'),
('f1f2f3f4-0002-4000-8000-000000000002', 'a1b2c3d4-2222-4000-8000-000000000002', 'b1b2c3d4-0004-4000-8000-000000000004', 4, 'Las hormigas culonas son toda una experiencia gastronómica. El mute santandereano estaba delicioso. Muy buen recorrido.', NOW() - INTERVAL '2 days'),

-- Juan Pablo reviews
('f1f2f3f4-0001-4000-8000-000000000003', 'a1b2c3d4-3333-4000-8000-000000000003', 'b1b2c3d4-0005-4000-8000-000000000005', 5, 'La cascada de Juan Curí es espectacular. El sendero está bien mantenido y la naturaleza es hermosa. Fuimos 4 y todos quedamos encantados.', NOW() - INTERVAL '4 days'),

-- Laura reviews
('f1f2f3f4-0001-4000-8000-000000000004', 'a1b2c3d4-4444-4000-8000-000000000004', 'b1b2c3d4-0007-4000-8000-000000000007', 4, 'La cueva es fascinante, las formaciones son impresionantes. Un poco claustrofóbico en algunos tramos pero vale la pena.', NOW() - INTERVAL '10 days'),
('f1f2f3f4-0002-4000-8000-000000000004', 'a1b2c3d4-4444-4000-8000-000000000004', 'b1b2c3d4-0008-4000-8000-000000000008', 5, 'Vimos más de 40 especies de aves en una mañana. El guía ornitólogo es un experto. Madrugar a las 5 AM vale totalmente la pena.', NOW() - INTERVAL '5 days'),
('f1f2f3f4-0003-4000-8000-000000000004', 'a1b2c3d4-4444-4000-8000-000000000004', 'b1b2c3d4-0010-4000-8000-000000000010', 4, 'La cabalgata por el Camino Real es hermosa. Los caballos están bien cuidados y Guane es un pueblo con mucha historia.', NOW() - INTERVAL '1 day'),

-- Andrés reviews
('f1f2f3f4-0001-4000-8000-000000000005', 'a1b2c3d4-5555-4000-8000-000000000005', 'b1b2c3d4-0009-4000-8000-000000000009', 5, 'El teleférico es una experiencia única. Las vistas del cañón son impresionantes y el parque tiene muchas actividades. Perfecto para toda la familia.', NOW() - INTERVAL '7 days'),

-- Cross-reviews (tourists reviewing other experiences)
('f1f2f3f4-0004-4000-8000-000000000002', 'a1b2c3d4-2222-4000-8000-000000000002', 'b1b2c3d4-0009-4000-8000-000000000009', 4, 'Muy bonito el teleférico, aunque los fines de semana hay mucha fila. Recomiendo ir entre semana.', NOW() - INTERVAL '15 days'),
('f1f2f3f4-0004-4000-8000-000000000003', 'a1b2c3d4-3333-4000-8000-000000000003', 'b1b2c3d4-0001-4000-8000-000000000001', 4, 'Excelente caminata pero hay que estar en buena forma física. El calor en el cañón es intenso, llevar mucha agua.', NOW() - INTERVAL '12 days'),
('f1f2f3f4-0004-4000-8000-000000000005', 'a1b2c3d4-5555-4000-8000-000000000005', 'b1b2c3d4-0006-4000-8000-000000000006', 5, 'Barichara es mágico. Las calles empedradas, los talleres artesanales y la vista al cañón hacen de este tour algo inolvidable.', NOW() - INTERVAL '14 days');
