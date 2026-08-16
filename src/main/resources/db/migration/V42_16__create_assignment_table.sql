CREATE TABLE IF NOT EXISTS assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course (id),
    teacher_id UUID NOT NULL REFERENCES teacher (id),
    academic_year_id UUID NOT NULL REFERENCES academic_year (id),
    CONSTRAINT uk_assignment_course_teacher_year
        UNIQUE (course_id, teacher_id, academic_year_id)
);

CREATE TABLE IF NOT EXISTS assignment_group (
    assignment_id UUID NOT NULL REFERENCES assignment (id) ON DELETE CASCADE,
    group_id UUID NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    PRIMARY KEY (assignment_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_assignment_course_year ON assignment (course_id, academic_year_id);