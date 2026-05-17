package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * HU-13 CA-5: envoltura de paginación.
 * { content, totalElements, totalPages, currentPage }
 *
 * Se construye manualmente en el servicio a partir del Page<T> de Spring Data
 * para exponer solo los campos que el cliente Flutter necesita,
 * sin acoplar la respuesta al formato interno de Spring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private long    totalElements;
    private int     totalPages;
    private int     currentPage;
}