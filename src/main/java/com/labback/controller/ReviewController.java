package com.labback.controller;

import com.labback.dto.CreateReviewRequest;
import com.labback.dto.PagedResponse;
import com.labback.dto.ReviewItemResponse;
import com.labback.dto.ReviewSummaryResponse;
import com.labback.service.ReviewService;
import com.labback.service.ReviewService.DuplicateReviewException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * HU-18 — Crear una reseña.
     * POST /reviews
     * Solo usuarios con role = CLIENT (HU-18 CA-3).
     * Body: { entrepreneurId, servicePostId, rating, comment }
     * Respuestas:
     *   201 → reseña creada
     *   400 → rating fuera de rango, servicio no pertenece al emprendedor, o datos inválidos
     *   403 → usuario no es CLIENT
     *   409 → ya existe una reseña del cliente para ese servicio (HU-18 CA-5)
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request) {

        try {
            ReviewItemResponse created =
                    reviewService.createReview(userDetails.getUsername(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);

        } catch (DuplicateReviewException e) {
            // HU-18 CA-5: reseña duplicada → 409 Conflict
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody(e.getMessage()));

        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(e.getMessage()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        }
    }

    /**
     * HU-19 CA-1 — Listado paginado de reseñas de un emprendedor.
     * GET /reviews/entrepreneur/{entrepreneurId}?page=0&size=10
     * Respuesta: { content[], totalElements, totalPages, currentPage }
     * Cada ítem: { rating, comment, createdAt, client: { fullName, photoUrl },
     *              servicePost: { id, title } }
     */
    @GetMapping("/entrepreneur/{entrepreneurId}")
    public ResponseEntity<?> getReviews(
            @PathVariable Integer entrepreneurId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            PagedResponse<ReviewItemResponse> reviews =
                    reviewService.getReviewsByEntrepreneur(entrepreneurId, page, size);
            return ResponseEntity.ok(reviews);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * HU-19 CA-2 — Resumen de calificaciones de un emprendedor.
     * GET /reviews/entrepreneur/{entrepreneurId}/summary
     * Respuesta: { averageRating, totalReviews,
     *              distribution: { "1": n, "2": n, "3": n, "4": n, "5": n } }
     * distribution siempre tiene las 5 claves (valor 0 si no hay reseñas de ese tipo).
     *
     * IMPORTANTE: esta ruta debe ir ANTES de /{entrepreneurId} para que Spring
     * no intente interpretar "summary" como un entrepreneurId numérico.
     * En este caso no hay conflicto porque están en paths distintos, pero
     * se documenta por claridad.
     */
    @GetMapping("/entrepreneur/{entrepreneurId}/summary")
    public ResponseEntity<?> getSummary(@PathVariable Integer entrepreneurId) {
        try {
            ReviewSummaryResponse summary = reviewService.getSummary(entrepreneurId);
            return ResponseEntity.ok(summary);

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

    private Map<String, Object> errorBody(String message) {
        return Map.of("success", false, "message", message);
    }
}