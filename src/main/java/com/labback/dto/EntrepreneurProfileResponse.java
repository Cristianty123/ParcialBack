package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HU-06: respuesta de GET /users/{id}
 * Solo aplica a usuarios ENTREPRENEUR.
 * averageRating y totalReviews se calculan en la capa de servicio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntrepreneurProfileResponse {
    private Integer id;
    private String  fullName;
    private String  photoUrl;
    private String  description;
    private String  address;
    private Double  averageRating;  // null si todavía no tiene reseñas
    private Long    totalReviews;
}