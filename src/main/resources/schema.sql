-- Schema for the Astradal Halloween Plague Plugin

-- Table to store active player infections
CREATE TABLE IF NOT EXISTS infections (
    -- The player's unique identifier
    player_uuid TEXT PRIMARY KEY NOT NULL,
    -- The current stage of the infection (1, 2, 3, etc.)
    stage INTEGER NOT NULL,
    -- Unix timestamp (milliseconds) of the initial infection
    infected_time INTEGER NOT NULL
);

-- Table to store staff-designated hospital regions
CREATE TABLE IF NOT EXISTS regions (
    -- Unique identifier for the region (e.g., "main_hospital")
    name TEXT PRIMARY KEY NOT NULL,
    -- World name
    world TEXT NOT NULL,
    -- Corner 1 coordinates
    x1 INTEGER NOT NULL,
    y1 INTEGER NOT NULL,
    z1 INTEGER NOT NULL,
    -- Corner 2 coordinates
    x2 INTEGER NOT NULL,
    y2 INTEGER NOT NULL,
    z2 INTEGER NOT NULL
);

-- Table to store player immunity expiration
CREATE TABLE IF NOT EXISTS immunity (
    -- The player's unique identifier
    player_uuid TEXT PRIMARY KEY NOT NULL,
    -- Unix timestamp (milliseconds) when immunity expires
    expires_time INTEGER NOT NULL
);