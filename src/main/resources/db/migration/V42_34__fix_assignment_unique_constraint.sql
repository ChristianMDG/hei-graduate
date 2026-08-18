-- BUG-14 FIX: la contrainte unique uk_assignment_course_teacher_year ne couvre pas semester_id.
-- Depuis la migration V42_26 (ADD COLUMN semester_id), un même enseignant peut être affecté
-- au même cours deux fois dans la même année académique (une fois pour S1, une fois pour S2).
-- Ce scénario est LÉGITIME (un prof peut enseigner son cours en S1 et en S2).
--
-- La nouvelle contrainte couvre (course_id, teacher_id, academic_year_id, semester_id) :
-- - Si semester_id IS NULL     → affectation "toute l'année" (un seul enregistrement possible)
-- - Si semester_id IS NOT NULL → affectation par semestre (S1 et S2 sont deux lignes distinctes)
--
-- PostgreSQL traite NULL comme des valeurs distinctes dans les contraintes UNIQUE.
-- Pour éviter les doublons sur semester_id IS NULL on utilise un index partiel.

-- Supprimer l'ancienne contrainte
ALTER TABLE assignment
    DROP CONSTRAINT IF EXISTS uk_assignment_course_teacher_year;

-- Nouvelle contrainte unique incluant semester_id (cas semester_id NOT NULL)
CREATE UNIQUE INDEX IF NOT EXISTS uk_assignment_course_teacher_year_semester
    ON assignment (course_id, teacher_id, academic_year_id, semester_id)
    WHERE semester_id IS NOT NULL;

-- Contrainte partielle pour semester_id IS NULL (affectation annuelle)
CREATE UNIQUE INDEX IF NOT EXISTS uk_assignment_course_teacher_year_no_semester
    ON assignment (course_id, teacher_id, academic_year_id)
    WHERE semester_id IS NULL;
