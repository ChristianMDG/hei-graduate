-- MCD fix #11: an exam was only linked to COURS + ANNEE_UNIVERSITAIRE, not to
-- SEMESTRE. Since course_track (V42_25) now lets the same course sit at
-- different semesters depending on the parcours, an exam without semester_id
-- is ambiguous about which semester it actually belongs to.

ALTER TABLE exam
    ADD COLUMN semester_id UUID REFERENCES semester (id);

-- Backfill (pass 1): attach each existing exam to the semester of its academic
-- year whose date range actually contains the exam date.
UPDATE exam e
SET semester_id = s.id
FROM semester s
WHERE e.semester_id IS NULL
  AND s.academic_year_id = e.academic_year_id
  AND e.date >= s.start_date
  AND e.date <= s.end_date;

-- Backfill (pass 2, fallback): an exam dated outside every semester of its
-- academic year (e.g. during an inter-semester break) would otherwise stay
-- NULL and block the NOT NULL constraint below. Attach it to the earliest
-- semester of that same academic year instead, same fallback strategy as
-- the course_track backfill in V42_25.
UPDATE exam e
SET semester_id = sub.semester_id
FROM (
    SELECT DISTINCT ON (s.academic_year_id) s.academic_year_id, s.id AS semester_id
    FROM semester s
    ORDER BY s.academic_year_id, s.start_date
) sub
WHERE e.semester_id IS NULL
  AND e.academic_year_id = sub.academic_year_id;

ALTER TABLE exam
    ALTER COLUMN semester_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_exam_semester ON exam (semester_id);
