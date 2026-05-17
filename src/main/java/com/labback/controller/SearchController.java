package com.labback.controller;

import com.labback.dto.PagedResponse;
import com.labback.dto.ServiceCardResponse;
import com.labback.dto.ServiceMapMarkerResponse;
import com.labback.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de búsqueda de servicios para el cliente (módulo 4).
 * Separado de ServiceController para mantener la responsabilidad única:
 * ServiceController gestiona el CRUD del emprendedor;
 * este controller gestiona la búsqueda/exploración del cliente.
 *
 * Nota sobre rutas: estas rutas (/services?...) y (/services/map)
 * coexisten con las de ServiceController porque Spring las diferencia
 * por los @RequestParam y el segmento /map.
 * El orden de @GetMapping("/map") antes de @GetMapping("/{id}") en
 * ServiceController ya garantiza que /map no se interprete como un ID.
 */
@RestController
@RequestMapping("/services")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * HU-13 — Buscar servicios por categoría y/o keyword.
     * GET /services?categoryId={id}&keyword={texto}&page=0&size=20
     *
     * Todos los parámetros son opcionales:
     * - Sin categoryId → todos los servicios ACTIVE.
     * - Sin keyword    → sin filtro de texto.
     * - Paginación por defecto: page=0, size=20.
     *
     * Respuesta: { content[], totalElements, totalPages, currentPage }
     */
    @GetMapping
    public ResponseEntity<PagedResponse<ServiceCardResponse>> searchServices(
            @RequestParam(required = false)               Integer categoryId,
            @RequestParam(required = false)               String  keyword,
            @RequestParam(defaultValue = "0")             int     page,
            @RequestParam(defaultValue = "20")            int     size) {

        // Limitar el tamaño de página para evitar peticiones abusivas
        int effectiveSize = Math.min(size, 50);

        PagedResponse<ServiceCardResponse> result =
                searchService.searchServices(categoryId, keyword, page, effectiveSize);

        return ResponseEntity.ok(result);
    }

    /**
     * HU-14 — Ver servicios en mapa dentro de un radio geográfico.
     * GET /services/map?lat={lat}&lng={lng}&radiusKm={km}&categoryId={id}
     *
     * lat y lng son obligatorios (sin ellos no se puede calcular distancia).
     * radiusKm por defecto 10 km; máximo 50 km (limitado en SearchService).
     * categoryId es opcional.
     *
     * Respuesta: lista de marcadores ligeros { id, title, latitude, longitude,
     *            category, entrepreneurFullName }.
     */
    @GetMapping("/map")
    public ResponseEntity<?> getMapMarkers(
            @RequestParam(required = false) Double  lat,
            @RequestParam(required = false) Double  lng,
            @RequestParam(defaultValue = "10")      double  radiusKm,
            @RequestParam(required = false)         Integer categoryId) {

        // lat y lng son requeridos lógicamente aunque Spring los acepta como opcionales
        // (para poder devolver un 400 descriptivo en lugar del error de conversión por defecto)
        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("success", false,
                            "message", "Los parámetros lat y lng son obligatorios"));
        }

        List<ServiceMapMarkerResponse> markers =
                searchService.getMapMarkers(lat, lng, radiusKm, categoryId);

        return ResponseEntity.ok(markers);
    }
}