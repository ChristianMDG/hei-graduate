-- BUG-06 FIX: Création de la table diploma_list (LISTE_DIPLOMES dans le MCD §14).
-- Le DiplomaExcelService générait et uploadait le fichier Excel sur S3 mais ne persistait jamais
-- le lien S3 en base, rendant impossible le re-téléchargement sans tout recalculer.
-- Cette table stocke le lien S3 pour chaque combinaison promotion/parcours.
CREATE TABLE IF NOT EXISTS diploma_list (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    promotion_id  UUID NOT NULL REFERENCES promotion (id),
    parcours_id   UUID NOT NULL REFERENCES parcours (id),
    generated_at  TIMESTAMP NOT NULL DEFAULT now(),
    url_s3        VARCHAR(500) NOT NULL,
    CONSTRAINT uk_diploma_list_promotion_parcours UNIQUE (promotion_id, parcours_id)
);

CREATE INDEX IF NOT EXISTS idx_diploma_list_promotion ON diploma_list (promotion_id);
