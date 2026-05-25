package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * HU-19 CA-2: resumen de calificaciones de un emprendedor.
 * { averageRating, totalReviews, distribution: { "1": n, "2": n, ..., "5": n } }
 * distribution siempre incluye las 5 claves aunque algún valor sea 0,
 * para que Flutter no tenga que manejar claves ausentes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResponse {
    private Double       averageRating;   // null si no hay reseñas
    private Long         totalReviews;
    private Map<Integer, Long> distribution; // clave: 1..5, valor: cantidad
}