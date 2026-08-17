ALTER TABLE semester
    ADD COLUMN academic_year_id UUID REFERENCES academic_year (id);

-- Backfill: attach each existing semester to the academic year whose date
-- range contains its start_date, so the column can then be made NOT NULL.
UPDATE semester s
SET academic_year_id = ay.id
FROM academic_year ay
WHERE s.academic_year_id IS NULL
  AND s.start_date >= ay.start_date
  AND s.start_date <= ay.end_date;

ALTER TABLE semester
    ALTER COLUMN academic_year_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_semester_academic_year ON semester (academic_year_id);
