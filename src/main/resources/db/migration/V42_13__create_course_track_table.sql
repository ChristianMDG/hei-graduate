CREATE TABLE IF NOT EXISTS course_track (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID NOT NULL REFERENCES course (id),
    track_id UUID NOT NULL REFERENCES parcours (id),
    academic_year_id UUID NOT NULL REFERENCES academic_year (id),
    mandatory BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_course_track_course_track_year
        UNIQUE (course_id, track_id, academic_year_id)
);

CREATE INDEX IF NOT EXISTS idx_course_track_track_year
    ON course_track (track_id, academic_year_id);