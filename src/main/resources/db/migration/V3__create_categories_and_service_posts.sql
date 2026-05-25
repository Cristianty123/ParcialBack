-- V3: Categorías y publicaciones de servicios

CREATE TABLE categories (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- Categorías iniciales del sistema (RF-09)
INSERT INTO categories (name) VALUES
    ('Diseño gráfico'),
    ('Reparaciones del hogar'),
    ('Tutorías y clases'),
    ('Belleza y cuidado personal'),
    ('Tecnología e informática'),
    ('Cocina y catering'),
    ('Fotografía y video'),
    ('Transporte y mudanzas'),
    ('Jardinería y limpieza'),
    ('Otros');

CREATE TABLE service_posts (
    id              SERIAL          PRIMARY KEY,
    title           VARCHAR(150)    NOT NULL,
    description     TEXT            NOT NULL,
    price           NUMERIC(12, 2),                    -- Opcional (RF-06)
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    address         VARCHAR(255),
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',  -- RF-08
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    entrepreneur_id INTEGER         NOT NULL REFERENCES users(id)      ON DELETE CASCADE,
    category_id     INTEGER         NOT NULL REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE INDEX idx_service_posts_entrepreneur ON service_posts(entrepreneur_id);
CREATE INDEX idx_service_posts_category     ON service_posts(category_id);
CREATE INDEX idx_service_posts_status       ON service_posts(status);

-- Índice espacial básico para búsquedas por ubicación (RF-11)
CREATE INDEX idx_service_posts_location ON service_posts(latitude, longitude)
    WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
