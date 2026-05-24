-- V6: Create reviews table
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tourist_id UUID NOT NULL,
    experience_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_tourist FOREIGN KEY (tourist_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_experience FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE,
    CONSTRAINT uk_reviews_tourist_experience UNIQUE (tourist_id, experience_id)
);

-- Index for tourist lookups
CREATE INDEX idx_reviews_tourist_id ON reviews(tourist_id);

-- Index for experience lookups (for calculating averages)
CREATE INDEX idx_reviews_experience_id ON reviews(experience_id);

-- Index for rating queries
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Index for created_at ordering
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);
