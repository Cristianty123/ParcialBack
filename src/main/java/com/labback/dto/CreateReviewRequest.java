package com.labback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** HU-18 CA-1: body de POST /reviews */
@Data
public class CreateReviewRequest {

    @NotNull(message = "El emprendedor es obligatorio")
    private Integer entrepreneurId;

    @NotNull(message = "El servicio es obligatorio")
    private Integer servicePostId;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    private String comment; // opcional
}