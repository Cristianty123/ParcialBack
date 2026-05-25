package com.labback.repository;

import com.labback.model.UploadedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UploadedImageRepository extends JpaRepository<UploadedImage, Integer> {

    /**
     * Busca el registro de una imagen por su filename (UUID + extensión).
     * Usado en DELETE /images/{filename} para:
     *   1. Verificar que la imagen existe en el sistema.
     *   2. Obtener el uploader para comparar con el usuario autenticado.
     */
    Optional<UploadedImage> findByFilename(String filename);
}