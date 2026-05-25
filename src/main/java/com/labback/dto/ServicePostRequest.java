package com.labback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Body de POST /services (HU-07) y PUT /services/{id} (HU-09).
 * title y description son obligatorios (HU-07 CA-3).
 * El resto son opcionales.
 */
@Data
public class ServicePostRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
    private String title;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres")
    private String description;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoryId;

    // Opcional (HU-07 CA-3)
    private BigDecimal price;

    private String  address;
    private Double  latitude;
    private Double  longitude;

    // Máximo 5 imágenes (validación de negocio en el servicio)
    private List<String> imageUrls;
}