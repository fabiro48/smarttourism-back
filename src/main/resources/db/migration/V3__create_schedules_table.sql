-- V3: Create schedules table
CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    experience_id UUID NOT NULL,
    day_of_week VARCHAR(20) NOT NULL CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available_slots INTEGER NOT NULL CHECK (available_slots >= 0),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedules_experience FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE,
    CONSTRAINT chk_schedules_time CHECK (end_time > start_time)
);

-- Index for experience lookups
CREATE INDEX idx_schedules_experience_id ON schedules(experience_id);

-- Index for day of week filtering
CREATE INDEX idx_schedules_day_of_week ON schedules(day_of_week);

-- Index for active schedules
CREATE INDEX idx_schedules_active ON schedules(active);

-- Index for available slots (for availability filtering)
CREATE INDEX idx_schedules_available_slots ON schedules(available_slots);
