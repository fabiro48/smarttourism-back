-- Create reviews table
CREATE TABLE reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tourist_id UUID NOT NULL,
    experience_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_tourist FOREIGN KEY (tourist_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_experience FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE,
    CONSTRAINT unique_tourist_experience UNIQUE (tourist_id, experience_id)
);

CREATE INDEX idx_reviews_tourist_id ON reviews(tourist_id);
CREATE INDEX idx_reviews_experience_id ON reviews(experience_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
