-- MCD v2 : COURS_PARCOURS est rattache au SEMESTRE (id_semestre), pas a
-- l'ANNEE_UNIVERSITAIRE. C'est ce qui permet a un meme cours d'etre place a
-- des semestres differents selon le parcours, et ce qui rend le semestre
-- d'un EXAMEN non ambigu (cf. correction #11 sur la table exam).
--
-- NOTE: comme pour V42_24, si des lignes "course_track" existent deja sur un
-- environnement, il faudra les rattacher manuellement a un semester_id avant
-- de rendre la colonne NOT NULL (ou vider la table si ce sont des donnees de
-- test).
ALTER TABLE course_track
    ADD COLUMN semester_id UUID REFERENCES semester (id);

ALTER TABLE course_track
    ALTER COLUMN semester_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_course_track_track_semester
    ON course_track (track_id, semester_id);

ALTER TABLE course_track DROP CONSTRAINT IF EXISTS uk_course_track_course_track_year;
ALTER TABLE course_track DROP COLUMN IF EXISTS academic_year_id;

ALTER TABLE course_track
    ADD CONSTRAINT uk_course_track_course_track_semester
        UNIQUE (course_id, track_id, semester_id);
