package com.labback.repository;

import com.labback.enums.ServiceStatus;
import com.labback.model.ServicePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePostRepository extends JpaRepository<ServicePost, Integer> {

    // ---------------------------------------------------------------
    // HU-08
    // ---------------------------------------------------------------

    @Query("""
            SELECT DISTINCT s FROM ServicePost s
            LEFT JOIN FETCH s.images
            LEFT JOIN FETCH s.category
            WHERE s.entrepreneur.id = :entrepreneurId
            ORDER BY s.createdAt DESC
            """)
    List<ServicePost> findByEntrepreneurIdWithImages(@Param("entrepreneurId") Integer entrepreneurId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.servicePost.id = :servicePostId")
    Double findAverageRatingByServicePostId(@Param("servicePostId") Integer servicePostId);

    // ---------------------------------------------------------------
    // HU-12
    // ---------------------------------------------------------------

    @Query("""
            SELECT s FROM ServicePost s
            LEFT JOIN FETCH s.images
            LEFT JOIN FETCH s.category
            LEFT JOIN FETCH s.entrepreneur
            WHERE s.id = :id
            """)
    Optional<ServicePost> findByIdWithDetails(@Param("id") Integer id);

    // ---------------------------------------------------------------
    // HU-13: búsqueda paginada con filtros opcionales
    //
    // countQuery separado para evitar el error de Hibernate al hacer
    // COUNT sobre un JOIN FETCH con colecciones (imágenes).
    // ---------------------------------------------------------------

    @Query(
            value = """
        SELECT DISTINCT s FROM ServicePost s
        JOIN FETCH s.category
        JOIN FETCH s.entrepreneur
        LEFT JOIN FETCH s.images
        WHERE s.status = com.labback.enums.ServiceStatus.ACTIVE
          AND (:categoryId IS NULL OR s.category.id = :categoryId)
          AND (
                CAST(:keyword AS string) IS NULL
                OR LOWER(s.title)       LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                OR LOWER(s.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
              )
        """,
            countQuery = """
        SELECT COUNT(DISTINCT s) FROM ServicePost s
        WHERE s.status = com.labback.enums.ServiceStatus.ACTIVE
          AND (:categoryId IS NULL OR s.category.id = :categoryId)
          AND (
                CAST(:keyword AS string) IS NULL
                OR LOWER(s.title)       LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
                OR LOWER(s.description) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%'))
              )
        """
    )
    Page<ServicePost> searchActive(
            @Param("categoryId") Integer categoryId,
            @Param("keyword")    String  keyword,
            Pageable             pageable
    );

    // ---------------------------------------------------------------
    // HU-14: servicios dentro de un radio usando Haversine (SQL nativo)
    //
    // JPQL no tiene SIN/COS/ACOS/RADIANS, por eso se usa nativeQuery.
    // La fórmula calcula distancia en km entre (lat,lng) del usuario
    // y las coordenadas de cada servicio. Solo devuelve ACTIVE con coords.
    // ---------------------------------------------------------------

    @Query(
            value = """
                SELECT s.*,
                       (6371 * ACOS(
                           COS(RADIANS(:lat)) * COS(RADIANS(s.latitude))
                           * COS(RADIANS(s.longitude) - RADIANS(:lng))
                           + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
                       )) AS distance_km
                FROM service_posts s
                WHERE s.status = 'ACTIVE'
                  AND s.latitude  IS NOT NULL
                  AND s.longitude IS NOT NULL
                  AND (:categoryId IS NULL OR s.category_id = :categoryId)
                  AND (6371 * ACOS(
                           COS(RADIANS(:lat)) * COS(RADIANS(s.latitude))
                           * COS(RADIANS(s.longitude) - RADIANS(:lng))
                           + SIN(RADIANS(:lat)) * SIN(RADIANS(s.latitude))
                       )) <= :radiusKm
                ORDER BY distance_km ASC
                """,
            nativeQuery = true
    )
    List<ServicePost> findActiveWithinRadius(
            @Param("lat")        double  lat,
            @Param("lng")        double  lng,
            @Param("radiusKm")   double  radiusKm,
            @Param("categoryId") Integer categoryId
    );
}