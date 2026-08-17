CREATE TABLE IF NOT EXISTS grade (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES student (id),
    exam_id UUID NOT NULL REFERENCES exam (id),
    value NUMERIC(4,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    CONSTRAINT uk_grade_student_exam UNIQUE (student_id, exam_id),
    CONSTRAINT chk_grade_value_range CHECK (value >= 0 AND value <= 20),
    CONSTRAINT chk_grade_status CHECK (status IN ('DRAFT', 'PUBLISHED'))
);

CREATE INDEX IF NOT EXISTS idx_grade_student_status ON grade (student_id, status);