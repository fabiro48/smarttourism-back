-- Create experiences table
CREATE TYPE difficulty_level AS ENUM ('EASY', 'MODERATE', 'HARD');

CREATE TABLE experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    duration INTEGER NOT NULL,
    difficulty difficulty_level NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    images TEXT[],
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_experiences_category ON experiences(category);
CREATE INDEX idx_experiences_location ON experiences(location);
CREATE INDEX idx_experiences_difficulty ON experiences(difficulty);
CREATE INDEX idx_experiences_active ON experiences(active);
