CREATE TABLE IF NOT EXISTS student_group (
    id         UUID PRIMARY KEY,
    reference  VARCHAR(20) NOT NULL,
    max_size   INT,
    CONSTRAINT uk_student_group_reference UNIQUE (reference)
);