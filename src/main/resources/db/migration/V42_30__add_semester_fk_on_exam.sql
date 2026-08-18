ALTER TABLE exam
    ADD COLUMN semester_id UUID REFERENCES semester (id);

UPDATE exam e
SET semester_id = s.id
FROM semester s
WHERE e.semester_id IS NULL
  AND s.academic_year_id = e.academic_year_id
  AND e.date >= s.start_date
  AND e.date <= s.end_date;

UPDATE exam e
SET semester_id = sub.semester_id
FROM (
    SELECT DISTINCT ON (s.academic_year_id) s.academic_year_id, s.id AS semester_id
    FROM semester s
    ORDER BY s.academic_year_id, s.start_date
) sub
WHERE e.semester_id IS NULL
  AND e.academic_year_id = sub.academic_year_id;

UPDATE exam e
SET semester_id = (
    SELECT s.id
    FROM semester s
    ORDER BY ABS(EXTRACT(EPOCH FROM (e.date - s.start_date)))
    LIMIT 1
)
WHERE e.semester_id IS NULL;

ALTER TABLE exam
    ALTER COLUMN semester_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_exam_semester ON exam (semester_id);