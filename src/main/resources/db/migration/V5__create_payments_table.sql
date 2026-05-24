-- V5: Create payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reservation_id UUID NOT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_reference VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED')),
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
);

-- Index for reservation lookups
CREATE INDEX idx_payments_reservation_id ON payments(reservation_id);

-- Index for transaction reference lookups
CREATE INDEX idx_payments_transaction_reference ON payments(transaction_reference);

-- Index for status filtering
CREATE INDEX idx_payments_status ON payments(status);

-- Index for payment date queries
CREATE INDEX idx_payments_payment_date ON payments(payment_date DESC);
