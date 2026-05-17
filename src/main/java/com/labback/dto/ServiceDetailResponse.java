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
 * HU-12 CA-1: respuesta de GET /services/{id}.
 * Incluye el emprendedor embebido con su averageRating.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDetailResponse {
    private Integer              id;
    private String               title;
    private String               description;
    private CategoryDto          category;
    private BigDecimal           price;
    private ServiceStatus        status;
    private String               address;
    private Double               latitude;
    private Double               longitude;
    private List<String>         imageUrls;
    private EntrepreneurSummaryDto entrepreneur;
    private LocalDateTime        createdAt;
}