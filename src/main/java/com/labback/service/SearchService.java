package com.labback.service;

import com.labback.dto.*;
import com.labback.model.Category;
import com.labback.model.ServicePost;
import com.labback.repository.CategoryRepository;
import com.labback.repository.ReviewRepository;
import com.labback.repository.ServicePostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final double MAX_RADIUS_KM = 50.0;

    private final ServicePostRepository servicePostRepository;
    private final ReviewRepository      reviewRepository;
    private final CategoryRepository    categoryRepository;

    public SearchService(ServicePostRepository servicePostRepository,
                         ReviewRepository reviewRepository,
                         CategoryRepository categoryRepository) {
        this.servicePostRepository = servicePostRepository;
        this.reviewRepository      = reviewRepository;
        this.categoryRepository    = categoryRepository;
    }

    // ---------------------------------------------------------------
    // HU-13: GET /services?categoryId=&keyword=&page=&size=
    // ---------------------------------------------------------------

    /**
     * Búsqueda paginada de servicios ACTIVE con filtros opcionales.
     *
     * - categoryId nulo → todas las categorías (HU-13 CA-2).
     * - keyword nulo o vacío → sin filtro de texto (HU-13 CA-3).
     * - Paginación por defecto: page=0, size=20, ordenado por createdAt DESC
     *   para mostrar los más recientes primero en el home.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ServiceCardResponse> searchServices(
            Integer categoryId,
            String  keyword,
            int     page,
            int     size) {

        // Normalizar keyword: cadena vacía → null para que la query JPQL
        // active el branch "IS NULL" y no filtre por texto
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ServicePost> resultPage = servicePostRepository.searchActive(
                categoryId, normalizedKeyword, pageable);

        List<ServiceCardResponse> cards = resultPage.getContent().stream()
                .map(this::toCardResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ServiceCardResponse>builder()
                .content(cards)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .currentPage(resultPage.getNumber())
                .build();
    }

    // ---------------------------------------------------------------
    // HU-14: GET /services/map?lat=&lng=&radiusKm=&categoryId=
    // ---------------------------------------------------------------

    /**
     * Devuelve marcadores de servicios ACTIVE dentro del radio indicado.
     *
     * - radiusKm se limita a MAX_RADIUS_KM (50 km) para evitar sobrecargas (HU-14 CA-4).
     * - Solo servicios con coordenadas definidas (HU-14 CA-2).
     * - categoryId es opcional (HU-14 CA-1).
     */
    @Transactional(readOnly = true)
    public List<ServiceMapMarkerResponse> getMapMarkers(
            double  lat,
            double  lng,
            double  radiusKm,
            Integer categoryId) {

        // HU-14 CA-4: cap de radio
        double effectiveRadius = Math.min(radiusKm, MAX_RADIUS_KM);

        List<ServicePost> posts = servicePostRepository.findActiveWithinRadius(
                lat, lng, effectiveRadius, categoryId);

        return posts.stream()
                .map(this::toMapMarker)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // HU-15: GET /categories
    // ---------------------------------------------------------------

    /**
     * Devuelve todas las categorías ordenadas por nombre.
     * Endpoint público (HU-15 CA-2) — sin lógica de negocio adicional.
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll(Sort.by("name")).stream()
                .map(c -> CategoryDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    /**
     * Construye el DTO de card para el home (HU-13).
     * thumbnail = primera imagen si existe, null si no tiene imágenes.
     * entrepreneurRating = promedio de calificaciones del emprendedor.
     */
    private ServiceCardResponse toCardResponse(ServicePost sp) {
        String thumbnail = sp.getImages().isEmpty()
                ? null
                : sp.getImages().get(0).getImageUrl();

        Double avgRating = reviewRepository
                .findAverageRatingByEntrepreneurId(sp.getEntrepreneur().getId());
        Double rounded = (avgRating != null)
                ? Math.round(avgRating * 10.0) / 10.0
                : null;

        return ServiceCardResponse.builder()
                .id(sp.getId())
                .title(sp.getTitle())
                .category(CategoryDto.builder()
                        .id(sp.getCategory().getId())
                        .name(sp.getCategory().getName())
                        .build())
                .price(sp.getPrice())
                .thumbnail(thumbnail)
                .entrepreneurRating(rounded)
                .entrepreneurName(sp.getEntrepreneur().getFullName())
                .build();
    }

    /** Construye el marcador ligero para el mapa (HU-14). */
    private ServiceMapMarkerResponse toMapMarker(ServicePost sp) {
        return ServiceMapMarkerResponse.builder()
                .id(sp.getId())
                .title(sp.getTitle())
                .latitude(sp.getLatitude())
                .longitude(sp.getLongitude())
                .category(CategoryDto.builder()
                        .id(sp.getCategory().getId())
                        .name(sp.getCategory().getName())
                        .build())
                .entrepreneurFullName(sp.getEntrepreneur().getFullName())
                .build();
    }
}