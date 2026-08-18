ALTER TABLE exam
    ADD COLUMN coefficient_numerator INTEGER,
    ADD COLUMN coefficient_denominator INTEGER;

ALTER TABLE exam DROP COLUMN coefficient;

ALTER TABLE exam
    ALTER COLUMN coefficient_numerator SET NOT NULL,
    ALTER COLUMN coefficient_denominator SET NOT NULL;

ALTER TABLE exam ADD CONSTRAINT chk_exam_coefficient_fraction
    CHECK (coefficient_numerator > 0 AND coefficient_denominator > 0 AND coefficient_numerator <= coefficient_denominator);