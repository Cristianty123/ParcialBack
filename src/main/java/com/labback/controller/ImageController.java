package com.labback.controller;

import com.labback.service.ImageStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;

/**
 * HU-20 — Gestión de imágenes.
 *
 * POST /images/upload   → recibe la imagen, la guarda y devuelve la URL pública.
 * GET  /images/{filename} → sirve la imagen almacenada (endpoint público).
 *
 * Flujo típico desde Flutter:
 *   1. Usuario selecciona imagen en el formulario de servicio.
 *   2. Flutter hace POST /images/upload con el archivo.
 *   3. Back responde { url: "http://.../" }.
 *   4. Flutter incluye esa URL en imageUrls[] al llamar POST/PUT /services.
 */
@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    /**
     * HU-20 CA-1 — Subir una imagen.
     * POST /images/upload
     * Requiere JWT (solo usuarios autenticados pueden subir imágenes).
     * Content-Type: multipart/form-data
     * Parámetro: "file" — el archivo de imagen.
     *
     * Respuesta 201: { "url": "http://localhost:8080/images/uuid.jpg" }
     * Respuesta 400: { "success": false, "message": "..." } si el archivo es inválido.
     *
     * Restricciones:
     *   - Tamaño máximo: 5 MB.
     *   - Tipos permitidos: image/jpeg, image/png, image/webp.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) {

        try {
            String url = imageStorageService.store(file);
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

    /**
     * HU-20 CA-2 — Servir una imagen almacenada.
     * GET /images/{filename}
     * Endpoint PÚBLICO (no requiere JWT) para que Flutter pueda cargar
     * las imágenes en widgets Image.network() sin necesidad de header Authorization.
     *
     * Respuesta 200: bytes de la imagen con el Content-Type correcto.
     * Respuesta 404: si el archivo no existe.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path    filePath = imageStorageService.load(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Detectar Content-Type según la extensión
            MediaType mediaType = resolveMediaType(filename);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (MalformedURLException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private MediaType resolveMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png"))  return MediaType.IMAGE_PNG;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG; // default para .jpg / .jpeg
    }
}