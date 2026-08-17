CREATE TABLE IF NOT EXISTS exam (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course (id),
    academic_year_id UUID NOT NULL REFERENCES academic_year (id),
    label VARCHAR(100) NOT NULL,
    coefficient NUMERIC(4,3) NOT NULL,
    CONSTRAINT chk_exam_coefficient_range CHECK (coefficient >= 0 AND coefficient <= 1)
);

CREATE INDEX IF NOT EXISTS idx_exam_course_year ON exam (course_id, academic_year_id);