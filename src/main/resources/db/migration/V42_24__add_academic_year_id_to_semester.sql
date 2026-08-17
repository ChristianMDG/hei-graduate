-- MCD v2, correction #4 : un semestre appartient toujours a exactement une
-- annee universitaire. Sans cette FK, rien ne garantit qu'un semestre est
-- propre a une annee, ce qui rend impossible le rattachement de
-- COURS_PARCOURS / EXAMEN / AFFECTATION au bon semestre (corrections #10, #11).
--
-- NOTE: si des lignes "semester" existent deja sur un environnement
-- (preprod), cette migration echouera tant qu'elles n'ont pas ete
-- rattachees a une academic_year existante (backfill manuel requis avant
-- de relancer, ou table videe si ce sont uniquement des donnees de test).
ALTER TABLE semester
    ADD COLUMN academic_year_id UUID REFERENCES academic_year (id);

ALTER TABLE semester
    ALTER COLUMN academic_year_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_semester_academic_year ON semester (academic_year_id);
