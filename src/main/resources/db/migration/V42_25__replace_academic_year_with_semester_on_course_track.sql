ALTER TABLE course_track
    ADD COLUMN semester_id UUID REFERENCES semester (id);

UPDATE course_track ct
SET semester_id = sub.semester_id
FROM (
    SELECT DISTINCT ON (s.academic_year_id) s.academic_year_id, s.id AS semester_id
    FROM semester s
    ORDER BY s.academic_year_id, s.start_date
) sub
WHERE ct.semester_id IS NULL
  AND ct.academic_year_id = sub.academic_year_id;

ALTER TABLE course_track
    ALTER COLUMN semester_id SET NOT NULL;

ALTER TABLE course_track
    DROP CONSTRAINT IF EXISTS uk_course_track_course_track_year;

ALTER TABLE course_track
    ADD CONSTRAINT uk_course_track_course_track_semester
        UNIQUE (course_id, track_id, semester_id);

ALTER TABLE course_track
    DROP COLUMN academic_year_id;

CREATE INDEX IF NOT EXISTS idx_course_track_track_semester
    ON course_track (track_id, semester_id);