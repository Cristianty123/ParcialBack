package com.labback.controller;

import com.labback.service.ImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gestión de imágenes: subida, descarga y borrado.
 *
 * POST   /images/upload          → sube la imagen, registra ownership, devuelve URL
 * GET    /images/{filename}      → sirve la imagen (público, sin JWT)
 * DELETE /images/{filename}      → borra archivo + registro (solo el uploader)
 *
 * Flujo para el cliente Flutter:
 *   1. Usuario selecciona imagen en el formulario.
 *   2. Flutter hace POST /images/upload → obtiene la URL.
 *   3a. Si publica el servicio: incluye la URL en imageUrls[] → la URL llega a service_images.
 *   3b. Si descarta la imagen ANTES de publicar (Escenario 1):
 *       Flutter llama DELETE /images/{filename} → limpia disco y BD.
 *   4. Si edita el servicio y elimina la imagen (Escenario 2):
 *       Flutter detecta las URLs quitadas y llama DELETE /images/{filename}
 *       por cada una → limpia disco y BD.
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    // ---------------------------------------------------------------
    // POST /images/upload — subir imagen
    // ---------------------------------------------------------------

    /**
     * Recibe la imagen, la guarda en disco y registra el ownership en BD.
     * Requiere JWT (solo usuarios autenticados pueden subir imágenes).
     *
     * CAMBIO respecto a la versión anterior: ahora recibe @AuthenticationPrincipal
     * para poder registrar quién subió la imagen en uploaded_images.
     *
     * Content-Type: multipart/form-data
     * Parámetro:    "file" — el archivo de imagen
     *
     * Respuesta 201: { "url": "http://localhost:8080/images/uuid.jpg" }
     * Respuesta 400: { "success": false, "message": "..." } si el archivo es inválido
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {

        try {
            // store() ahora recibe el username para registrar ownership
            String url = imageStorageService.store(file, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("url", url));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error interno al procesar la imagen"));
        }
    }

    // ---------------------------------------------------------------
    // GET /images/{filename} — servir imagen (público)
    // ---------------------------------------------------------------

    /**
     * Sirve la imagen almacenada.
     * Endpoint PÚBLICO (no requiere JWT) para que Flutter pueda usar
     * Image.network() sin header Authorization.
     *
     * Respuesta 200: bytes de la imagen con el Content-Type correcto
     * Respuesta 404: si el archivo no existe en disco
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path     filePath = imageStorageService.load(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(resolveMediaType(filename))
                    .body(resource);

        } catch (MalformedURLException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ---------------------------------------------------------------
    // DELETE /images/{filename} — borrar imagen (solo el uploader)
    // ---------------------------------------------------------------

    /**
     * Borra el archivo del disco y su registro en uploaded_images.
     * Requiere JWT. Solo el usuario que subió la imagen puede borrarla.
     *
     * Cuándo lo llama Flutter:
     *   - Escenario 1: usuario quita la imagen del formulario ANTES de publicar.
     *   - Escenario 2: usuario edita un servicio y elimina una imagen existente
     *     (justo antes o después de llamar PUT /services/{id}).
     *
     * Respuesta 204: imagen borrada correctamente (sin cuerpo)
     * Respuesta 403: el solicitante no es el uploader de esta imagen
     * Respuesta 404: la imagen no existe en el registro de uploaded_images
     * Respuesta 500: error al borrar el archivo del disco
     */
    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<Map<String, Object>> deleteImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String filename) {

        try {
            imageStorageService.delete(filename, userDetails.getUsername());
            // 204 No Content: borrado exitoso, sin cuerpo de respuesta
            return ResponseEntity.noContent().build();

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (IllegalArgumentException e) {
            // Imagen no encontrada en el registro de uploaded_images
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al eliminar la imagen: " + e.getMessage()));
        }
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}