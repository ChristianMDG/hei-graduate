-- MCD v2, correction #11 : un cours peut etre positionne a des semestres
-- differents selon le parcours (via course_track), donc EXAMEN doit preciser
-- explicitement son semestre en plus de son annee universitaire, sinon on ne
-- sait pas a quel semestre appartient un examen donne.
--
-- NOTE: comme pour V42_24/V42_25, backfill manuel requis si des lignes
-- "exam" existent deja sur un environnement avant de rendre la colonne
-- NOT NULL.
ALTER TABLE exam
    ADD COLUMN semester_id UUID REFERENCES semester (id);

ALTER TABLE exam
    ALTER COLUMN semester_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_exam_semester ON exam (semester_id);
