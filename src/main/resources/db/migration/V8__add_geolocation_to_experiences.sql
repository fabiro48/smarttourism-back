-- V8: Add geolocation columns to experiences table
ALTER TABLE experiences
    ADD COLUMN latitude DOUBLE PRECISION NOT NULL DEFAULT 7.1254,
    ADD COLUMN longitude DOUBLE PRECISION NOT NULL DEFAULT -73.1198;
