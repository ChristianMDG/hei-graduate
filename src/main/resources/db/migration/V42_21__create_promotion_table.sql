CREATE TABLE promotion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    label VARCHAR(255) NOT NULL,
    final_academic_year_id UUID NOT NULL REFERENCES academic_year(id),
    CONSTRAINT uk_promotion_final_academic_year UNIQUE (final_academic_year_id)
);