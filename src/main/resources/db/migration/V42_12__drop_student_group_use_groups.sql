-- StudentGroup was a duplicate of Group (GROUPE domain owned by Membre 1).
-- Enrollment must reference the canonical groups table instead.

ALTER TABLE enrollment
    DROP CONSTRAINT IF EXISTS enrollment_student_group_id_fkey;

ALTER TABLE enrollment
    RENAME COLUMN student_group_id TO group_id;

ALTER TABLE enrollment
    ADD CONSTRAINT fk_enrollment_group FOREIGN KEY (group_id) REFERENCES groups (id);

DROP TABLE IF EXISTS student_group;
