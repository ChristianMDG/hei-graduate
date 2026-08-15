CREATE TABLE IF NOT EXISTS parcours (
    id      UUID PRIMARY KEY,
    code    VARCHAR(10) NOT NULL,
    label   VARCHAR(255) NOT NULL,
    active  BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_parcours_code UNIQUE (code)
);