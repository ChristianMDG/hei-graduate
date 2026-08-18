ALTER TABLE assignment
    ADD COLUMN semester_id UUID REFERENCES semester (id);

CREATE INDEX IF NOT EXISTS idx_assignment_semester ON assignment (semester_id);
