-- V2: Ampliar la tabla users existente con campos de perfil y rol
-- La tabla base (id, username, password) ya existe desde V1

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS role        VARCHAR(20)  NOT NULL DEFAULT 'CLIENT',
    ADD COLUMN IF NOT EXISTS full_name   VARCHAR(100),
    ADD COLUMN IF NOT EXISTS photo_url   TEXT,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS latitude    DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS longitude   DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS address     VARCHAR(255);

-- Eliminar el default temporal una vez aplicada la migración
ALTER TABLE users ALTER COLUMN role DROP DEFAULT;
