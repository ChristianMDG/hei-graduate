CREATE TABLE IF NOT EXISTS academic_year (
    id          UUID PRIMARY KEY,
    label       VARCHAR(20) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    level       VARCHAR(10) NOT NULL
);