package com.labback.service;

import com.labback.dto.CreateReviewRequest;
import com.labback.dto.PagedResponse;
import com.labback.dto.ReviewItemResponse;
import com.labback.dto.ReviewSummaryResponse;
import com.labback.enums.Role;
import com.labback.model.Review;
import com.labback.model.ServicePost;
import com.labback.model.User;
import com.labback.repository.ReviewRepository;
import com.labback.repository.ServicePostRepository;
import com.labback.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository      reviewRepository;
    private final UserRepository        userRepository;
    private final ServicePostRepository servicePostRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         UserRepository userRepository,
                         ServicePostRepository servicePostRepository) {
        this.reviewRepository      = reviewRepository;
        this.userRepository        = userRepository;
        this.servicePostRepository = servicePostRepository;
    }

    // ---------------------------------------------------------------
    // HU-18: POST /reviews — crear reseña
    // ---------------------------------------------------------------

    /**
     * Crea una reseña de un cliente hacia un emprendedor por un servicio concreto.
     *
     * Validaciones en orden:
     *   CA-3: solo CLIENT puede crear reseñas.
     *   CA-4: el servicePostId debe pertenecer al entrepreneurId indicado.
     *   CA-5: un cliente no puede calificar dos veces el mismo servicio (→ 409).
     */
    @Transactional
    public ReviewItemResponse createReview(String username, CreateReviewRequest req) {

        User client = findByUsernameOrThrow(username);

        // HU-18 CA-3: solo CLIENT
        if (client.getRole() != Role.CLIENT) {
            throw new AccessDeniedException("Solo los clientes pueden dejar reseñas");
        }

        // Verificar que el emprendedor existe y es ENTREPRENEUR
        User entrepreneur = userRepository.findById(req.getEntrepreneurId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Emprendedor no encontrado con id: " + req.getEntrepreneurId()));

        if (entrepreneur.getRole() != Role.ENTREPRENEUR) {
            throw new IllegalArgumentException(
                    "El usuario con id " + req.getEntrepreneurId() + " no es un emprendedor");
        }

        // HU-18 CA-4: el servicio debe pertenecer al emprendedor indicado
        if (!reviewRepository.servicePostBelongsToEntrepreneur(
                req.getServicePostId(), req.getEntrepreneurId())) {
            throw new IllegalArgumentException(
                    "El servicio indicado no pertenece al emprendedor especificado");
        }

        // HU-18 CA-5: un cliente no puede calificar el mismo servicio dos veces
        if (reviewRepository.existsByClientIdAndServicePostId(
                client.getId(), req.getServicePostId())) {
            throw new DuplicateReviewException(
                    "Ya has dejado una reseña para este servicio");
        }

        ServicePost servicePost = servicePostRepository.findById(req.getServicePostId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Servicio no encontrado con id: " + req.getServicePostId()));

        Review review = Review.builder()
                .rating(req.getRating())
                .comment(req.getComment())
                .client(client)
                .entrepreneur(entrepreneur)
                .servicePost(servicePost)
                .build();

        Review saved = reviewRepository.save(review);
        return toReviewItemResponse(saved);
    }

    // ---------------------------------------------------------------
    // HU-19 CA-1: GET /reviews/entrepreneur/{id}?page=&size=
    // ---------------------------------------------------------------

    /**
     * Lista paginada de reseñas recibidas por un emprendedor, más recientes primero.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ReviewItemResponse> getReviewsByEntrepreneur(
            Integer entrepreneurId, int page, int size) {

        assertEntrepreneurExists(entrepreneurId);

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> resultPage =
                reviewRepository.findByEntrepreneurIdPaged(entrepreneurId, pageable);

        List<ReviewItemResponse> content = resultPage.getContent().stream()
                .map(this::toReviewItemResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ReviewItemResponse>builder()
                .content(content)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .currentPage(resultPage.getNumber())
                .build();
    }

    // ---------------------------------------------------------------
    // HU-19 CA-2: GET /reviews/entrepreneur/{id}/summary
    // ---------------------------------------------------------------

    /**
     * Resumen con promedio, total y distribución 1-5.
     * La distribución siempre incluye las 5 claves (con 0 si no hay reseñas
     * de ese valor) para que Flutter no tenga que manejar claves ausentes.
     */
    @Transactional(readOnly = true)
    public ReviewSummaryResponse getSummary(Integer entrepreneurId) {
        assertEntrepreneurExists(entrepreneurId);

        Double avgRating    = reviewRepository.findAverageRatingByEntrepreneurId(entrepreneurId);
        Long   totalReviews = reviewRepository.countByEntrepreneurId(entrepreneurId);

        Double roundedAvg = (avgRating != null)
                ? Math.round(avgRating * 10.0) / 10.0
                : null;

        // Inicializar distribución con 0 en todos los valores (1..5)
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        // Sobrescribir con los valores reales que devuelve la query
        reviewRepository.findRatingDistribution(entrepreneurId)
                .forEach(row -> {
                    Integer rating = ((Number) row[0]).intValue();
                    Long    count  = ((Number) row[1]).longValue();
                    distribution.put(rating, count);
                });

        return ReviewSummaryResponse.builder()
                .averageRating(roundedAvg)
                .totalReviews(totalReviews)
                .distribution(distribution)
                .build();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private User findByUsernameOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    private void assertEntrepreneurExists(Integer entrepreneurId) {
        User user = userRepository.findById(entrepreneurId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Emprendedor no encontrado con id: " + entrepreneurId));
        if (user.getRole() != Role.ENTREPRENEUR) {
            throw new IllegalArgumentException(
                    "El usuario con id " + entrepreneurId + " no es un emprendedor");
        }
    }

    private ReviewItemResponse toReviewItemResponse(Review r) {
        return ReviewItemResponse.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .client(ReviewItemResponse.ClientDto.builder()
                        .fullName(r.getClient().getFullName())
                        .photoUrl(r.getClient().getPhotoUrl())
                        .build())
                .servicePost(ReviewItemResponse.ServiceTitleDto.builder()
                        .id(r.getServicePost().getId())
                        .title(r.getServicePost().getTitle())
                        .build())
                .build();
    }

    // Excepción interna para el 409 de reseña duplicada (HU-18 CA-5)
    public static class DuplicateReviewException extends RuntimeException {
        public DuplicateReviewException(String message) {
            super(message);
        }
    }
}