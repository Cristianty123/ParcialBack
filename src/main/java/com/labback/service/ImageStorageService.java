package com.labback.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * HU-20: Servicio de almacenamiento local de imágenes.
 *
 * Flujo:
 *   1. Flutter manda la imagen como multipart/form-data a POST /images/upload.
 *   2. Este servicio la guarda en el directorio configurado en app.upload-dir.
 *   3. Devuelve la URL pública donde el back la sirve (GET /images/{filename}).
 *   4. Flutter usa esa URL en imageUrls[] al crear / editar un servicio.
 *
 * Tipos permitidos: JPEG, PNG, WebP (MAX_SIZE_MB de tamaño máximo).
 */
@Service
public class ImageStorageService {

    private static final long MAX_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_CONTENT_TYPES =
            Arrays.asList("image/jpeg", "image/png", "image/webp");

    /** Directorio base donde se guardan los archivos. Configurable en application.properties. */
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /**
     * URL base del servidor para construir la URL pública de cada imagen.
     * Ejemplo: "http://localhost:8080"  o  "https://api.tudominio.com"
     */
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Guarda el archivo recibido y devuelve la URL pública donde se puede obtener.
     *
     * @param file archivo multipart enviado por Flutter
     * @return URL completa, ej. "http://localhost:8080/images/a1b2c3d4.jpg"
     */
    public String store(MultipartFile file) {
        validate(file);

        String extension  = getExtension(file.getOriginalFilename());
        String filename   = UUID.randomUUID() + extension;
        Path   targetPath = resolveUploadDir().resolve(filename);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }

        return baseUrl + "/images/" + filename;
    }

    /**
     * Devuelve el Path físico de un archivo almacenado por su nombre.
     * Usado por ImageController para servir la imagen.
     *
     * @param filename nombre del archivo (ej. "a1b2c3d4.jpg")
     * @return Path al archivo en disco
     */
    public Path load(String filename) {
        // Prevenir path traversal (ej. "../../etc/passwd")
        Path resolved = resolveUploadDir().resolve(filename).normalize();
        if (!resolved.startsWith(resolveUploadDir())) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }
        return resolved;
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    /** Crea el directorio de uploads si no existe y devuelve su Path absoluto. */
    private Path resolveUploadDir() {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("No se pudo crear el directorio de uploads: " + e.getMessage(), e);
            }
        }
        return dir;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo de 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Se aceptan: JPEG, PNG, WebP");
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return ".jpg"; // fallback
    }
}