-- V2: Create experiences table
CREATE TABLE experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(200) NOT NULL,
    duration INTEGER,
    difficulty VARCHAR(20) CHECK (difficulty IN ('EASY', 'MODERATE', 'HARD', 'EXTREME')),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    images TEXT[],
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for category filtering
CREATE INDEX idx_experiences_category ON experiences(category);

-- Index for location filtering (supports LIKE queries)
CREATE INDEX idx_experiences_location ON experiences(location);

-- Index for difficulty filtering
CREATE INDEX idx_experiences_difficulty ON experiences(difficulty);

-- Index for price range filtering
CREATE INDEX idx_experiences_price ON experiences(price);

-- Index for active experiences
CREATE INDEX idx_experiences_active ON experiences(active);
