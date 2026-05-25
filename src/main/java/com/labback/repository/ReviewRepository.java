package com.labback.repository;

import com.labback.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    // ---------------------------------------------------------------
    // Usado desde HU-06, HU-12, HU-13 (ya existía)
    // ---------------------------------------------------------------

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.entrepreneur.id = :entrepreneurId")
    Double findAverageRatingByEntrepreneurId(@Param("entrepreneurId") Integer entrepreneurId);

    Long countByEntrepreneurId(Integer entrepreneurId);

    // ---------------------------------------------------------------
    // HU-18 CA-5: verificar si el cliente ya calificó ese servicio
    // ---------------------------------------------------------------

    boolean existsByClientIdAndServicePostId(Integer clientId, Integer servicePostId);

    // ---------------------------------------------------------------
    // HU-18 CA-4: verificar que el servicePost pertenece al entrepreneur
    // ---------------------------------------------------------------

    @Query("""
            SELECT COUNT(s) > 0 FROM ServicePost s
            WHERE s.id = :servicePostId
              AND s.entrepreneur.id = :entrepreneurId
            """)
    boolean servicePostBelongsToEntrepreneur(
            @Param("servicePostId")  Integer servicePostId,
            @Param("entrepreneurId") Integer entrepreneurId
    );

    // ---------------------------------------------------------------
    // HU-19 CA-1: listado paginado de reseñas de un emprendedor
    // Carga cliente y servicePost en el mismo query (evita N+1).
    // ---------------------------------------------------------------

    @Query("""
            SELECT r FROM Review r
            JOIN FETCH r.client
            JOIN FETCH r.servicePost
            WHERE r.entrepreneur.id = :entrepreneurId
            ORDER BY r.createdAt DESC
            """)
    Page<Review> findByEntrepreneurIdPaged(
            @Param("entrepreneurId") Integer entrepreneurId,
            Pageable pageable
    );

    // ---------------------------------------------------------------
    // HU-19 CA-2: distribución de estrellas (1 a 5)
    // Devuelve pares [rating, count] para construir el resumen.
    // ---------------------------------------------------------------

    @Query("""
            SELECT r.rating, COUNT(r)
            FROM Review r
            WHERE r.entrepreneur.id = :entrepreneurId
            GROUP BY r.rating
            ORDER BY r.rating ASC
            """)
    List<Object[]> findRatingDistribution(@Param("entrepreneurId") Integer entrepreneurId);
}