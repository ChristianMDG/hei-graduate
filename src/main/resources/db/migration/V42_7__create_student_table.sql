CREATE TABLE IF NOT EXISTS student (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    student_number  VARCHAR(20) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255) NOT NULL,
    birth_date      DATE,
    enrollment_date DATE NOT NULL,
    status          VARCHAR(50) NOT NULL,
    CONSTRAINT uk_student_number UNIQUE (student_number),
    CONSTRAINT uk_student_user UNIQUE (user_id)
);