CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference VARCHAR(50) NOT NULL,
    capacity INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_groups_reference UNIQUE (reference)
);

CREATE INDEX IF NOT EXISTS idx_groups_active ON groups (active);