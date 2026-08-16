CREATE TABLE IF NOT EXISTS semester (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    expected_credits INTEGER NOT NULL DEFAULT 30,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_semester_dates CHECK (start_date < end_date)
);

CREATE INDEX IF NOT EXISTS idx_semester_active ON semester (active);