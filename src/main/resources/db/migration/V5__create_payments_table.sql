-- Create payments table
CREATE TYPE payment_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'FAILED');

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_reference VARCHAR(255) NOT NULL UNIQUE,
    status payment_status NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);
CREATE INDEX idx_payments_transaction_reference ON payments(transaction_reference);
CREATE INDEX idx_payments_status ON payments(status);
