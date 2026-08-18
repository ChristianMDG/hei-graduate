ALTER TABLE exam
    ADD COLUMN coefficient_numerator INTEGER,
    ADD COLUMN coefficient_denominator INTEGER;

-- Backfill : convertit l'ancien coefficient decimal (NUMERIC(4,3), ex: 0.400)
-- en fraction entiere avant de supprimer la colonne source. Denominateur fixe
-- de 1000 pour preserver exactement les 3 decimales d'origine (ex: 0.4 ->
-- 400/1000, 0.125 -> 125/1000), sans arrondi/perte de precision. Sans ce
-- backfill, coefficient_numerator/denominator restent NULL pour toute ligne
-- existante et l'ALTER COLUMN ... SET NOT NULL plus bas echoue avec
-- SQLSTATE 23502 ("contains null values").
UPDATE exam
SET coefficient_numerator = ROUND(coefficient * 1000)::INTEGER,
    coefficient_denominator = 1000
WHERE coefficient_numerator IS NULL;

ALTER TABLE exam DROP COLUMN coefficient;

ALTER TABLE exam
    ALTER COLUMN coefficient_numerator SET NOT NULL,
    ALTER COLUMN coefficient_denominator SET NOT NULL;

ALTER TABLE exam ADD CONSTRAINT chk_exam_coefficient_fraction
    CHECK (coefficient_numerator > 0 AND coefficient_denominator > 0 AND coefficient_numerator <= coefficient_denominator);
