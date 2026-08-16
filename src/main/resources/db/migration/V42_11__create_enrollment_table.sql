CREATE TABLE IF NOT EXISTS enrollment (
    id                UUID PRIMARY KEY,
    student_id        UUID NOT NULL REFERENCES student(id),
    parcours_id       UUID NOT NULL REFERENCES parcours(id),
    student_group_id  UUID NOT NULL REFERENCES student_group(id),
    start_date        DATE NOT NULL,
    end_date          DATE
);

CREATE INDEX IF NOT EXISTS idx_enrollment_student ON enrollment (student_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_enrollment_one_active_per_student
    ON enrollment (student_id) WHERE end_date IS NULL;