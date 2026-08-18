-- MCD fix #2 (gap): grade_history.changed_by_user_id has existed since V42_20
-- as a plain "UUID NOT NULL" column, but was never given an actual FK
-- constraint to users(id) -- unlike grade.entered_by_user_id (V42_27), which
-- already does this correctly for the equivalent "who touched this row" case.
--
-- Without the constraint, the referential integrity required by the corrected
-- MCD (HISTORIQUE_NOTE -> UTILISATEUR "modifie") isn't actually enforced at
-- the DB level: a changed_by_user_id could reference a user that never
-- existed, silently breaking the traceability the MCD introduced this column
-- for in the first place (cf. cahier des charges §9, "Modifié par : ...").
--
-- Note: if any existing grade_history row already has a changed_by_user_id
-- that doesn't match a row in users, this ALTER TABLE will fail -- same
-- pre-existing risk as any other retrofitted FK in this migration history
-- (V42_24, V42_25, V42_28), flagged here for visibility.

ALTER TABLE grade_history
    ADD CONSTRAINT fk_grade_history_changed_by_user
        FOREIGN KEY (changed_by_user_id) REFERENCES users (id);

CREATE INDEX IF NOT EXISTS idx_grade_history_changed_by ON grade_history (changed_by_user_id);
