package com.labback.dto;

import com.labback.enums.ServiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-08 CA-3: item de la lista "mis servicios".
 * También se reutiliza como cuerpo de respuesta en HU-07, HU-09 y HU-11
 * para devolver el servicio creado/actualizado sin el bloque completo del emprendedor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceSummaryResponse {
    private Integer       id;
    private String        title;
    private CategoryDto   category;
    private ServiceStatus status;
    private BigDecimal    price;
    private LocalDateTime createdAt;
    private List<String>  imageUrls;
    private Double        averageRating;
}