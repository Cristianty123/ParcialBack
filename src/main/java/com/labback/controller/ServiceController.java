package com.labback.controller;

import com.labback.dto.*;
import com.labback.service.ServicePostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
public class ServiceController {

    private final ServicePostService servicePostService;

    public ServiceController(ServicePostService servicePostService) {
        this.servicePostService = servicePostService;
    }

    /**
     * HU-07 — Publicar un servicio.
     * POST /services
     * Solo ENTREPRENEUR (HU-07 CA-2).
     * Respuesta 201 con el servicio creado (HU-07 CA-5).
     */
    @PostMapping
    @PreAuthorize("hasRole('ENTREPRENEUR')")
    public ResponseEntity<?> createService(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ServicePostRequest request) {

        try {
            ServiceSummaryResponse created = servicePostService.createService(
                    userDetails.getUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (IllegalArgumentException e) {
            // categoryId inválido → HU-07 CA-6
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /**
     * HU-08 — Ver mis servicios.
     * GET /services/my-services
     * Solo ENTREPRENEUR (HU-08 CA-4).
     * IMPORTANTE: esta ruta debe declararse ANTES de GET /{id}
     * para que Spring no la interprete como id = "my-services".
     */
    @GetMapping("/my-services")
    @PreAuthorize("hasRole('ENTREPRENEUR')")
    public ResponseEntity<List<ServiceSummaryResponse>> getMyServices(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ServiceSummaryResponse> services = servicePostService
                .getMyServices(userDetails.getUsername());
        return ResponseEntity.ok(services);
    }

    /**
     * HU-09 — Editar un servicio.
     * PUT /services/{id}
     * Solo el dueño (HU-09 CA-2); 403 si no lo es.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ENTREPRENEUR')")
    public ResponseEntity<?> updateService(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody ServicePostRequest request) {

        try {
            ServiceSummaryResponse updated = servicePostService.updateService(
                    userDetails.getUsername(), id, request);
            return ResponseEntity.ok(updated);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /**
     * HU-10 — Eliminar un servicio.
     * DELETE /services/{id}
     * Solo el dueño (HU-10 CA-2).
     * Respuesta 204 No Content al eliminar (HU-10 CA-3).
     * Respuesta 404 si no existe (HU-10 CA-4).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ENTREPRENEUR')")
    public ResponseEntity<?> deleteService(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id) {

        try {
            servicePostService.deleteService(userDetails.getUsername(), id);
            return ResponseEntity.noContent().build();

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * HU-11 — Cambiar estado de un servicio.
     * PATCH /services/{id}/status
     * Solo el dueño (HU-11 CA-2).
     * Respuesta 200 con el servicio actualizado (HU-11 CA-3).
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ENTREPRENEUR')")
    public ResponseEntity<?> changeStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody ServiceStatusRequest request) {

        try {
            ServiceSummaryResponse updated = servicePostService.changeStatus(
                    userDetails.getUsername(), id, request.getStatus());
            return ResponseEntity.ok(updated);

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * HU-12 — Ver detalle de un servicio.
     * GET /services/{id}
     * Accesible por CLIENT y ENTREPRENEUR autenticados.
     * Clientes solo ven servicios ACTIVE; el dueño ve también INACTIVE (HU-12 CA-2).
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id) {

        try {
            ServiceDetailResponse detail = servicePostService.getServiceDetail(
                    userDetails.getUsername(), id);
            return ResponseEntity.ok(detail);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ---------------------------------------------------------------
    // Manejo de errores de validación (@Valid)
    // ---------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(errorBody(message));
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    private java.util.Map<String, Object> errorBody(String message) {
        return java.util.Map.of("success", false, "message", message);
    }
}