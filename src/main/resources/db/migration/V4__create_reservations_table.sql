-- V4: Create reservations table
CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tourist_id UUID NOT NULL,
    experience_id UUID NOT NULL,
    schedule_id UUID NOT NULL,
    reservation_date DATE NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'NO_SHOW')),
    expiration_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_tourist FOREIGN KEY (tourist_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_experience FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

-- Index for tourist lookups (for "my reservations")
CREATE INDEX idx_reservations_tourist_id ON reservations(tourist_id);

-- Index for experience lookups (admin queries)
CREATE INDEX idx_reservations_experience_id ON reservations(experience_id);

-- Index for schedule lookups (slot management)
CREATE INDEX idx_reservations_schedule_id ON reservations(schedule_id);

-- Index for status filtering
CREATE INDEX idx_reservations_status ON reservations(status);

-- Composite index for expiration job (status + expiration_date)
CREATE INDEX idx_reservations_expiration ON reservations(status, expiration_date);

-- Index for reservation date filtering
CREATE INDEX idx_reservations_date ON reservations(reservation_date);

-- Index for created_at ordering
CREATE INDEX idx_reservations_created_at ON reservations(created_at DESC);
