-- Create reservations table
CREATE TYPE reservation_status AS ENUM ('PENDING_PAYMENT', 'CONFIRMED', 'CANCELLED', 'EXPIRED');

CREATE TABLE reservations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tourist_id UUID NOT NULL,
    experience_id UUID NOT NULL,
    schedule_id UUID NOT NULL,
    reservation_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status reservation_status NOT NULL DEFAULT 'PENDING_PAYMENT',
    expiration_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_tourist FOREIGN KEY (tourist_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_experience FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

CREATE INDEX idx_reservations_tourist_id ON reservations(tourist_id);
CREATE INDEX idx_reservations_experience_id ON reservations(experience_id);
CREATE INDEX idx_reservations_schedule_id ON reservations(schedule_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_expiration_date ON reservations(expiration_date);
CREATE INDEX idx_reservations_created_at ON reservations(created_at);
