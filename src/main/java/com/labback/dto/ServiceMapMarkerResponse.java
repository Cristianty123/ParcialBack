package com.labback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HU-14 CA-3: payload mínimo por marcador del mapa.
 * { id, title, latitude, longitude, category, entrepreneur: { fullName } }
 * Se mantiene ligero a propósito: el mapa puede tener decenas de marcadores
 * y no necesita imágenes ni descripción completa.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMapMarkerResponse {
    private Integer     id;
    private String      title;
    private Double      latitude;
    private Double      longitude;
    private CategoryDto category;
    private String      entrepreneurFullName;
}