CREATE TABLE IF NOT EXISTS course (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_reference VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    credits INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_course_reference UNIQUE (course_reference)
);

CREATE INDEX IF NOT EXISTS idx_course_active ON course (active);