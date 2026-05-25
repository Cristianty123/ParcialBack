package com.labback.service;

import com.labback.dto.*;
import com.labback.enums.Role;
import com.labback.enums.ServiceStatus;
import com.labback.model.Category;
import com.labback.model.ServiceImage;
import com.labback.model.ServicePost;
import com.labback.model.User;
import com.labback.repository.CategoryRepository;
import com.labback.repository.ReviewRepository;
import com.labback.repository.ServicePostRepository;
import com.labback.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicePostService {

    private final ServicePostRepository servicePostRepository;
    private final CategoryRepository    categoryRepository;
    private final ReviewRepository      reviewRepository;
    private final UserRepository        userRepository;

    public ServicePostService(ServicePostRepository servicePostRepository,
                              CategoryRepository categoryRepository,
                              ReviewRepository reviewRepository,
                              UserRepository userRepository) {
        this.servicePostRepository = servicePostRepository;
        this.categoryRepository    = categoryRepository;
        this.reviewRepository      = reviewRepository;
        this.userRepository        = userRepository;
    }

    // ---------------------------------------------------------------
    // HU-07: POST /services — publicar un servicio
    // ---------------------------------------------------------------

    /**
     * Crea un nuevo servicio para el emprendedor autenticado.
     * Solo ENTREPRENEUR puede llamar este método (verificado en el controller con @PreAuthorize).
     * Valida que la categoría exista (HU-07 CA-6).
     * Crea las ServiceImage asociadas al mismo tiempo (cascade).
     */
    @Transactional
    public ServiceSummaryResponse createService(String username, ServicePostRequest req) {
        User entrepreneur = findUserByUsername(username);
        Category category = findCategoryOrThrow(req.getCategoryId());

        ServicePost servicePost = ServicePost.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .price(req.getPrice())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(ServiceStatus.ACTIVE)   // HU-07 CA-4: siempre ACTIVE al crear
                .entrepreneur(entrepreneur)
                .category(category)
                .build();

        // Agregar imágenes (máx. 5, validado aquí para dar mensaje claro)
        if (req.getImageUrls() != null) {
            if (req.getImageUrls().size() > 5) {
                throw new IllegalArgumentException("Se permiten máximo 5 imágenes por servicio");
            }
            req.getImageUrls().forEach(url ->
                    servicePost.getImages().add(
                            ServiceImage.builder()
                                    .imageUrl(url)
                                    .servicePost(servicePost)
                                    .build()
                    )
            );
        }

        ServicePost saved = servicePostRepository.save(servicePost);
        return toSummaryResponse(saved, null); // nuevo servicio: sin ratings aún
    }

    // ---------------------------------------------------------------
    // HU-08: GET /services/my-services — mis servicios
    // ---------------------------------------------------------------

    /**
     * Lista todos los servicios (activos e inactivos) del emprendedor autenticado.
     * Incluye el averageRating de cada servicio (HU-08 CA-3).
     */
    @Transactional(readOnly = true)
    public List<ServiceSummaryResponse> getMyServices(String username) {
        User entrepreneur = findUserByUsername(username);

        return servicePostRepository
                .findByEntrepreneurIdWithImages(entrepreneur.getId())
                .stream()
                .map(sp -> {
                    Double avg = servicePostRepository.findAverageRatingByServicePostId(sp.getId());
                    return toSummaryResponse(sp, avg);
                })
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // HU-09: PUT /services/{id} — editar un servicio
    // ---------------------------------------------------------------

    /**
     * Edita un servicio existente.
     * Solo el dueño puede editarlo (HU-09 CA-2).
     * Reemplaza completamente las imágenes por las del request.
     */
    @Transactional
    public ServiceSummaryResponse updateService(String username, Integer serviceId, ServicePostRequest req) {
        User entrepreneur = findUserByUsername(username);
        ServicePost servicePost = findServiceOrThrow(serviceId);

        assertOwnership(servicePost, entrepreneur.getId());

        Category category = findCategoryOrThrow(req.getCategoryId());

        servicePost.setTitle(req.getTitle());
        servicePost.setDescription(req.getDescription());
        servicePost.setPrice(req.getPrice());
        servicePost.setAddress(req.getAddress());
        servicePost.setLatitude(req.getLatitude());
        servicePost.setLongitude(req.getLongitude());
        servicePost.setCategory(category);

        // Reemplazar imágenes: limpiar las actuales y agregar las nuevas
        // orphanRemoval = true en ServicePost se encarga de borrar las viejas
        servicePost.getImages().clear();
        if (req.getImageUrls() != null) {
            if (req.getImageUrls().size() > 5) {
                throw new IllegalArgumentException("Se permiten máximo 5 imágenes por servicio");
            }
            req.getImageUrls().forEach(url ->
                    servicePost.getImages().add(
                            ServiceImage.builder()
                                    .imageUrl(url)
                                    .servicePost(servicePost)
                                    .build()
                    )
            );
        }

        ServicePost saved = servicePostRepository.save(servicePost);
        Double avg = servicePostRepository.findAverageRatingByServicePostId(saved.getId());
        return toSummaryResponse(saved, avg);
    }

    // ---------------------------------------------------------------
    // HU-10: DELETE /services/{id} — eliminar un servicio
    // ---------------------------------------------------------------

    /**
     * Elimina el servicio y sus imágenes en cascada (HU-10 CA-1).
     * Solo el dueño puede eliminarlo (HU-10 CA-2).
     */
    @Transactional
    public void deleteService(String username, Integer serviceId) {
        User entrepreneur = findUserByUsername(username);
        ServicePost servicePost = findServiceOrThrow(serviceId);
        assertOwnership(servicePost, entrepreneur.getId());
        servicePostRepository.delete(servicePost);
    }

    // ---------------------------------------------------------------
    // HU-11: PATCH /services/{id}/status — cambiar estado
    // ---------------------------------------------------------------

    /**
     * Cambia el estado (ACTIVE / INACTIVE) del servicio.
     * Solo el dueño puede cambiar el estado (HU-11 CA-2).
     */
    @Transactional
    public ServiceSummaryResponse changeStatus(String username, Integer serviceId, ServiceStatus newStatus) {
        User entrepreneur = findUserByUsername(username);
        ServicePost servicePost = findServiceOrThrow(serviceId);
        assertOwnership(servicePost, entrepreneur.getId());

        servicePost.setStatus(newStatus);
        ServicePost saved = servicePostRepository.save(servicePost);
        Double avg = servicePostRepository.findAverageRatingByServicePostId(saved.getId());
        return toSummaryResponse(saved, avg);
    }

    // ---------------------------------------------------------------
    // HU-12: GET /services/{id} — detalle de un servicio
    // ---------------------------------------------------------------

    /**
     * Devuelve el detalle completo de un servicio.
     * HU-12 CA-2: clientes solo ven servicios ACTIVE.
     *             El dueño puede ver sus propios servicios INACTIVE.
     */
    @Transactional(readOnly = true)
    public ServiceDetailResponse getServiceDetail(String username, Integer serviceId) {
        User requester = findUserByUsername(username);
        ServicePost sp = servicePostRepository.findByIdWithDetails(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con id: " + serviceId));

        boolean isOwner = sp.getEntrepreneur().getId().equals(requester.getId());

        // Cliente o emprendedor que NO es dueño: solo puede ver servicios activos
        if (!isOwner && sp.getStatus() != ServiceStatus.ACTIVE) {
            throw new IllegalArgumentException("Servicio no encontrado con id: " + serviceId);
        }

        // Rating del emprendedor para el bloque entrepreneur (HU-12 CA-1)
        Double avgEntrepreneur = reviewRepository
                .findAverageRatingByEntrepreneurId(sp.getEntrepreneur().getId());
        Double roundedAvg = (avgEntrepreneur != null)
                ? Math.round(avgEntrepreneur * 10.0) / 10.0
                : null;

        return ServiceDetailResponse.builder()
                .id(sp.getId())
                .title(sp.getTitle())
                .description(sp.getDescription())
                .category(CategoryDto.builder()
                        .id(sp.getCategory().getId())
                        .name(sp.getCategory().getName())
                        .build())
                .price(sp.getPrice())
                .status(sp.getStatus())
                .address(sp.getAddress())
                .latitude(sp.getLatitude())
                .longitude(sp.getLongitude())
                .imageUrls(sp.getImages().stream()
                        .map(ServiceImage::getImageUrl)
                        .collect(Collectors.toList()))
                .entrepreneur(EntrepreneurSummaryDto.builder()
                        .id(sp.getEntrepreneur().getId())
                        .fullName(sp.getEntrepreneur().getFullName())
                        .photoUrl(sp.getEntrepreneur().getPhotoUrl())
                        .averageRating(roundedAvg)
                        .build())
                .createdAt(sp.getCreatedAt())
                .build();
    }

    // ---------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    private ServicePost findServiceOrThrow(Integer serviceId) {
        return servicePostRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado con id: " + serviceId));
    }

    private Category findCategoryOrThrow(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Categoría no encontrada con id: " + categoryId));
    }

    /**
     * Verifica que el usuario autenticado sea el dueño del servicio.
     * Lanza AccessDeniedException (→ 403) si no lo es.
     */
    private void assertOwnership(ServicePost servicePost, Integer requesterId) {
        if (!servicePost.getEntrepreneur().getId().equals(requesterId)) {
            throw new AccessDeniedException(
                    "No tienes permiso para modificar este servicio");
        }
    }

    private ServiceSummaryResponse toSummaryResponse(ServicePost sp, Double avgRating) {
        Double rounded = (avgRating != null)
                ? Math.round(avgRating * 10.0) / 10.0
                : null;

        return ServiceSummaryResponse.builder()
                .id(sp.getId())
                .title(sp.getTitle())
                .category(CategoryDto.builder()
                        .id(sp.getCategory().getId())
                        .name(sp.getCategory().getName())
                        .build())
                .status(sp.getStatus())
                .price(sp.getPrice())
                .createdAt(sp.getCreatedAt())
                .imageUrls(sp.getImages().stream()
                        .map(ServiceImage::getImageUrl)
                        .collect(Collectors.toList()))
                .averageRating(rounded)
                .build();
    }
}