ALTER TABLE grade
    ADD COLUMN entered_by_user_id UUID REFERENCES users (id);

CREATE INDEX IF NOT EXISTS idx_grade_entered_by ON grade (entered_by_user_id);
