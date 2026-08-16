CREATE TABLE IF NOT EXISTS teacher (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users(id),
    last_name       VARCHAR(255) NOT NULL,
    first_name      VARCHAR(255) NOT NULL,
    specialty       VARCHAR(255),
    contract_type   VARCHAR(50),
    CONSTRAINT uk_teacher_user UNIQUE (user_id)
);