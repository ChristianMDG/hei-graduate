-- BUG-05 FIX: Ajout de la colonne entered_at sur la table grade.
-- Le MCD (entité NOTE §8 cahier des charges) exige : "une date de saisie ou de modification".
-- Le modèle Grade.java avait entered_by_user_id mais pas de date — ce correctif comble ce manque.
-- DEFAULT now() pour les lignes existantes afin d'éviter l'erreur "contains null values".
ALTER TABLE grade
    ADD COLUMN IF NOT EXISTS entered_at TIMESTAMP NOT NULL DEFAULT now();
