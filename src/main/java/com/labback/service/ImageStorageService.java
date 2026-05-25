package com.labback.service;

import com.labback.model.UploadedImage;
import com.labback.model.User;
import com.labback.repository.UploadedImageRepository;
import com.labback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * Gestión de imágenes subidas al servidor local.
 *
 * Cambios respecto a la versión anterior:
 *   - store() ahora recibe el username del uploader y persiste un registro
 *     en uploaded_images para garantizar ownership al borrar.
 *   - Nuevo  delete() que verifica que el solicitante sea el uploader
 *     antes de borrar el archivo del disco y el registro de la tabla.
 *
 * Esto resuelve los dos escenarios de imágenes huérfanas:
 *   Escenario 1 — imagen subida pero descartada antes de publicar el servicio.
 *   Escenario 2 — imagen de un servicio existente eliminada al editar.
 *   En ambos casos Flutter llama DELETE /images/{filename} y el back limpia.
 */
@Service
public class ImageStorageService {

    private static final long         MAX_SIZE_BYTES    = 5L * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_TYPES     =
            Arrays.asList("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final UploadedImageRepository uploadedImageRepository;
    private final UserRepository          userRepository;

    public ImageStorageService(UploadedImageRepository uploadedImageRepository,
                               UserRepository userRepository) {
        this.uploadedImageRepository = uploadedImageRepository;
        this.userRepository          = userRepository;
    }

    // ---------------------------------------------------------------
    // POST /images/upload  →  store()
    // ---------------------------------------------------------------

    /**
     * Guarda el archivo en disco y registra el ownership en uploaded_images.
     *
     * @param file     archivo multipart enviado por Flutter
     * @param username username del usuario autenticado (extraído del JWT)
     * @return URL pública de la imagen, ej. "http://localhost:8080/images/a1b2c3.jpg"
     */
    @Transactional
    public String store(MultipartFile file, String username) {
        validate(file);

        User uploader = findUserOrThrow(username);

        // Generar nombre único para evitar colisiones y evitar exponer el nombre original
        String extension = getExtension(file.getOriginalFilename());
        String filename  = UUID.randomUUID() + extension;
        Path   target    = resolveUploadDir().resolve(filename);

        // 1. Guardar archivo en disco
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }

        // 2. Registrar ownership en BD (si falla se hace rollback y el archivo
        //    no queda registrado, pero ya está en disco; es una situación extrema
        //    que se puede resolver con la tarea de limpieza futura)
        uploadedImageRepository.save(
                UploadedImage.builder()
                        .filename(filename)
                        .uploadedBy(uploader)
                        .build()
        );

        return baseUrl + "/images/" + filename;
    }

    // ---------------------------------------------------------------
    // DELETE /images/{filename}  →  delete()
    // ---------------------------------------------------------------

    /**
     * Elimina el archivo del disco y su registro de uploaded_images,
     * verificando primero que el solicitante sea el uploader original.
     *
     * Flujo:
     *   1. Busca el registro por filename → 404 si no existe.
     *   2. Compara uploadedBy con el usuario autenticado → 403 si no coincide.
     *   3. Borra el archivo físico del disco.
     *   4. Borra el registro de uploaded_images (en la misma transacción).
     *
     * Si el archivo físico ya no existe en disco (inconsistencia) se sigue
     * adelante y se borra el registro de BD para mantener la consistencia.
     *
     * @param filename el UUID+extensión (último segmento de la URL), ej. "a1b2c3.jpg"
     * @param username username del usuario autenticado
     */
    @Transactional
    public void delete(String filename, String username) {
        // 1. Verificar que el registro existe
        UploadedImage record = uploadedImageRepository.findByFilename(filename)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Imagen no encontrada: " + filename));

        // 2. Verificar ownership
        if (!record.getUploadedBy().getUsername().equals(username)) {
            throw new AccessDeniedException(
                    "No tienes permiso para eliminar esta imagen");
        }

        // 3. Borrar del disco (tolerante a que el archivo ya no exista en disco)
        Path filePath = resolveUploadDir().resolve(filename).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log pero no abortar: si el archivo ya no está en disco,
            // lo importante es limpiar el registro de BD
            throw new RuntimeException("Error al eliminar el archivo del disco: " + e.getMessage(), e);
        }

        // 4. Borrar registro de BD (el @Transactional garantiza atomicidad con el paso anterior)
        uploadedImageRepository.delete(record);
    }

    // ---------------------------------------------------------------
    // load() — usado por ImageController para servir imágenes (GET)
    // ---------------------------------------------------------------

    /**
     * Resuelve el Path físico de un archivo por su filename.
     * Incluye protección contra path traversal.
     */
    public Path load(String filename) {
        Path resolved = resolveUploadDir().resolve(filename).normalize();
        if (!resolved.startsWith(resolveUploadDir())) {
            throw new IllegalArgumentException("Nombre de archivo no válido");
        }
        return resolved;
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    private Path resolveUploadDir() {
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(
                        "No se pudo crear el directorio de uploads: " + e.getMessage(), e);
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
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Se aceptan: JPEG, PNG, WebP");
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        return ".jpg";
    }

    private User findUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }
}