-- V4: Imágenes, chat y reseñas

-- Imágenes de los servicios (RF-06)
CREATE TABLE service_images (
    id              SERIAL  PRIMARY KEY,
    image_url       TEXT    NOT NULL,
    service_post_id INTEGER NOT NULL REFERENCES service_posts(id) ON DELETE CASCADE
);

CREATE INDEX idx_service_images_post ON service_images(service_post_id);

-- Mensajes de chat privado (RF-13, RNF-05)
CREATE TABLE chat_messages (
    id          SERIAL    PRIMARY KEY,
    content     TEXT      NOT NULL,
    sent_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    is_read     BOOLEAN   NOT NULL DEFAULT FALSE,
    sender_id   INTEGER   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id INTEGER   NOT NULL REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT chk_no_self_chat CHECK (sender_id <> receiver_id)
);

-- Índice para recuperar el historial de conversación entre dos usuarios
-- El canal se identifica por el par ordenado (min_id, max_id)
CREATE INDEX idx_chat_conversation ON chat_messages(
    LEAST(sender_id, receiver_id),
    GREATEST(sender_id, receiver_id),
    sent_at
);

-- Reseñas y calificaciones (RF-16, RF-17, RF-18, RF-19, RN-02)
CREATE TABLE reviews (
    id              SERIAL    PRIMARY KEY,
    rating          SMALLINT  NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment         TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    client_id       INTEGER   NOT NULL REFERENCES users(id)         ON DELETE CASCADE,
    entrepreneur_id INTEGER   NOT NULL REFERENCES users(id)         ON DELETE CASCADE,
    service_post_id INTEGER   NOT NULL REFERENCES service_posts(id) ON DELETE CASCADE,

    -- RN-02: un cliente solo puede dejar UNA reseña por servicio
    CONSTRAINT uq_review_client_service UNIQUE (client_id, service_post_id)
);

CREATE INDEX idx_reviews_entrepreneur ON reviews(entrepreneur_id);
CREATE INDEX idx_reviews_service_post ON reviews(service_post_id);
