CREATE TABLE IF NOT EXISTS transcript (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES student (id),
    academic_year_id UUID NOT NULL REFERENCES academic_year (id),
    semester_id UUID REFERENCES semester (id),
    type VARCHAR(20) NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT now(),
    url_s3 VARCHAR(500) NOT NULL,
    average_grade NUMERIC(4,2),
    obtained_credits INTEGER NOT NULL,
    expected_credits INTEGER NOT NULL,
    year_validated BOOLEAN NOT NULL,
    email_sent BOOLEAN NOT NULL DEFAULT false,
    email_sent_at TIMESTAMP,
    CONSTRAINT chk_transcript_type CHECK (type IN ('COMPLET', 'PROVISOIRE'))
);

CREATE INDEX IF NOT EXISTS idx_transcript_student ON transcript (student_id);
CREATE INDEX IF NOT EXISTS idx_transcript_academic_year ON transcript (academic_year_id);