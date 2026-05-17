package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * HU-13 CA-4: datos que Flutter necesita para renderizar cada card del home.
 * thumbnail → primera imagen del servicio (null si no tiene).
 * entrepreneurRating → promedio de calificaciones del emprendedor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCardResponse {
    private Integer      id;
    private String       title;
    private CategoryDto  category;
    private BigDecimal   price;
    private String       thumbnail;        // primera imageUrl, o null
    private Double       entrepreneurRating;
    private String       entrepreneurName;
}