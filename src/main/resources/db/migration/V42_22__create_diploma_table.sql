CREATE TABLE diploma (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES student(id),
    promotion_id UUID NOT NULL REFERENCES promotion(id),
    parcours_id UUID NOT NULL REFERENCES parcours(id),
    obtained_date DATE NOT NULL,
    overall_average NUMERIC(4,2) NOT NULL,
    rank INTEGER NOT NULL,
    mention VARCHAR(20) NOT NULL,
    CONSTRAINT uk_diploma_student_promotion UNIQUE (student_id, promotion_id)
);

CREATE INDEX idx_diploma_promotion_parcours ON diploma (promotion_id, parcours_id);