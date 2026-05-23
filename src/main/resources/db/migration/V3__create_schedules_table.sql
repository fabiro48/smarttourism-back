-- Create schedules table
CREATE TYPE day_of_week_enum AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY');

CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    experience_id UUID NOT NULL,
    day_of_week day_of_week_enum NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available_slots INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_schedules_experience FOREIGN KEY (experience_id) REFERENCES experiences(id) ON DELETE CASCADE
);

CREATE INDEX idx_schedules_experience_id ON schedules(experience_id);
CREATE INDEX idx_schedules_day_of_week ON schedules(day_of_week);
CREATE INDEX idx_schedules_active ON schedules(active);
