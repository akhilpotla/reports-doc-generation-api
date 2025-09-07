CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    email VARCHAR(320) UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);
DO $$ BEGIN IF NOT EXISTS (
    SELECT 1
    FROM pg_type
    WHERE typname = 'api_key_status'
) THEN CREATE TYPE api_key_status AS ENUM ('ACTIVE', 'INACTIVE');
END IF;
END $$^;
CREATE TABLE IF NOT EXISTS api (
    id SERIAL PRIMARY KEY,
    api_token VARCHAR(64) NOT NULL UNIQUE,
    status api_key_status NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES "user"(id)
);
