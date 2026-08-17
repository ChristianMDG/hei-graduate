CREATE TABLE IF NOT EXISTS grade_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    grade_id UUID NOT NULL REFERENCES grade (id),
    old_value NUMERIC(4,2) NOT NULL,
    new_value NUMERIC(4,2) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    changed_by_user_id UUID NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_grade_history_grade_id ON grade_history (grade_id);