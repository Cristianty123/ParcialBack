-- V6: Tabla de seguimiento de imágenes subidas.
--
-- Problema que resuelve:
--   Escenario 1: usuario sube imagen → la quita del formulario antes de publicar
--                → el archivo quedaba huérfano en disco para siempre.
--   Escenario 2: usuario edita servicio y elimina imagen → service_images borra
--                el registro via orphanRemoval pero el archivo físico persistía.
--
-- Solución:
--   1. Al subir, se registra (filename, uploader, fecha) en esta tabla.
--   2. DELETE /images/{filename} verifica que el solicitante sea el uploader
--      antes de borrar el archivo del disco y el registro de aquí.
--   3. Flutter llama DELETE en los dos escenarios (quitar antes de guardar /
--      quitar al editar), manteniendo el disco siempre limpio.

CREATE TABLE uploaded_images (
    id          SERIAL       PRIMARY KEY,
    filename    VARCHAR(255) NOT NULL UNIQUE,   -- UUID + extensión, ej. "a1b2c3.jpg"
    uploaded_by INTEGER      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_uploaded_images_user ON uploaded_images(uploaded_by);
